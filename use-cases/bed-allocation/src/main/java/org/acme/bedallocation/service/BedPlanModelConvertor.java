package org.acme.bedallocation.service;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.domain.BedPlanConstraintProperties;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.acme.bedallocation.dto.input.BedPlanConfigOverrides;
import org.acme.bedallocation.dto.input.BedPlanInput;
import org.acme.bedallocation.dto.input.RoomInputDTO;
import org.acme.bedallocation.dto.input.StayInputDTO;
import org.acme.bedallocation.dto.output.BedPlanOutput;
import org.acme.bedallocation.dto.output.StayOutputDTO;

@ApplicationScoped
public class BedPlanModelConvertor
        implements ModelConvertor<HardMediumSoftScore, BedPlanInput, BedPlanConfigOverrides, BedPlan, BedPlanOutput> {

    @Override
    public BedPlanInput applyOutputToInput(BedPlanInput modelInput, BedPlanOutput modelOutput) {
        Map<String, StayOutputDTO> outputStays =
                modelOutput.stays().stream().collect(Collectors.toMap(StayOutputDTO::id, stay -> stay));
        List<StayInputDTO> updatedStays = modelInput.stays().stream()
                .map(stay -> {
                    StayOutputDTO solved = outputStays.get(stay.id());
                    return solved == null ? stay : stay.withBedId(solved.bedId());
                })
                .collect(Collectors.toList());
        return modelInput.withStays(updatedStays);
    }

    @Override
    public BedPlan toSolverModel(BedPlanInput modelInput, ModelConfig<BedPlanConfigOverrides> modelConfig,
            Optional<BedPlanOutput> lastModelOutput) {
        List<Department> departments = new ArrayList<>();
        List<Room> rooms = new ArrayList<>();
        Map<String, Bed> bedMap = new LinkedHashMap<>();

        for (var departmentInputDto : modelInput.departments()) {
            Map<String, Integer> specialityToPrioMap = orEmpty(departmentInputDto.specialtyToPriority());

            var department = new Department(departmentInputDto.id(), departmentInputDto.name(),
                    departmentInputDto.minimumAge(), departmentInputDto.maximumAge(),
                    specialityToPrioMap);

            for (RoomInputDTO roomDto : departmentInputDto.rooms()) {
                var room = new Room(roomDto.id(), roomDto.name(), department, roomDto.capacity(),
                        roomDto.genderLimitation(), orEmpty(roomDto.equipments()));

                for (var bedInputDto : roomDto.beds()) {
                    var bed = new Bed(bedInputDto.id(), room);
                    bedMap.put(bed.id(), bed);
                }
            }
        }

        var stays = modelInput.stays().stream()
                .map(dto -> toStay(dto, bedMap))
                .toList();

        BedPlan bedPlan = new BedPlan(departments, rooms, new ArrayList<>(bedMap.values()), stays);
        applyConstraintWeightOverrides(bedPlan, modelConfig);
        applyLastOutput(stays, bedMap, lastModelOutput);
        return bedPlan;
    }


    @Override
    public BedPlanOutput toModelOutput(BedPlan solverModel) {
        var stays = solverModel.getStays().stream()
                .map(s -> new StayOutputDTO(s.getId(), s.getBed() == null ? null : s.getBed().id())).toList();
        return new BedPlanOutput(stays);
    }

    private static Stay toStay(StayInputDTO dto, Map<String, Bed> bedMap) {
        var bed = dto.bedId() == null ? null : require(bedMap, dto.bedId(), "bed");
        return new Stay(dto.id(), dto.patientName(), dto.patientGender(), dto.patientAge(),
                dto.patientPreferredMaximumRoomCapacity(), orEmpty(dto.patientRequiredEquipments()),
                orEmpty(dto.patientPreferredEquipments()), dto.arrivalDate(),
                dto.departureDate(), dto.specialty(), bed, Boolean.TRUE.equals(dto.pinned()));
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : unmodifiableList(list);
    }

    private static <T> Set<T> orEmpty(Set<T> set) {
        return set == null ? Set.of() : unmodifiableSet(set);
    }

    private static <T, Y> Map<T, Y> orEmpty(Map<T, Y> map) {
        return map == null ? Map.of() : unmodifiableMap(map);
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(BedPlan bedPlan, ModelConfig<BedPlanConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, BedPlanConstraintProperties.PREFERRED_MAXIMUM_ROOM_CAPACITY,
                overrides.preferredMaximumRoomCapacityWeight());
        putIfPresent(weights, BedPlanConstraintProperties.DEPARTMENT_SPECIALTY, overrides.departmentSpecialtyWeight());
        putIfPresent(weights, BedPlanConstraintProperties.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY,
                overrides.departmentSpecialtyNotFirstPriorityWeight());
        putIfPresent(weights, BedPlanConstraintProperties.PREFERRED_PATIENT_EQUIPMENT,
                overrides.preferredPatientEquipmentWeight());
        if (!weights.isEmpty()) {
            bedPlan.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Stay> stays, Map<String, Bed> bedMap, Optional<BedPlanOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        var stayMap = stays.stream().collect(Collectors.toMap(Stay::getId, stay -> stay));
        for (var solved : lastModelOutput.get().stays()) {
            Stay stay = stayMap.get(solved.id());
            if (stay == null || stay.getBed() != null || stay.isPinned() || solved.bedId() == null) {
                continue;
            }
            Bed bed = bedMap.get(solved.bedId());
            if (bed != null) {
                stay.setBed(bed);
            }
        }
    }
}
