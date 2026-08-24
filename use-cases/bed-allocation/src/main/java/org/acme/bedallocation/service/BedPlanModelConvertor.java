package org.acme.bedallocation.service;

import java.time.LocalDate;
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
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedPlanConfigOverrides;
import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.BedPlanOutput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;

@ApplicationScoped
public class BedPlanModelConvertor
        implements ModelConvertor<HardMediumSoftScore, BedPlanInput, BedPlanConfigOverrides, BedPlan, BedPlanOutput> {

    @Override
    public BedPlanInput applyOutputToInput(BedPlanInput modelInput, BedPlanOutput modelOutput) {
        Map<String, StayDTO> outputStays =
                modelOutput.stays().stream().collect(Collectors.toMap(StayDTO::id, stay -> stay));
        List<StayDTO> updatedStays = modelInput.stays().stream()
                .map(stay -> {
                    StayDTO solved = outputStays.get(stay.id());
                    return solved == null ? stay : stay.withBedId(solved.bedId());
                })
                .collect(Collectors.toList());
        return modelInput.withStays(updatedStays);
    }

    @Override
    public BedPlan toSolverModel(BedPlanInput modelInput, ModelConfig<BedPlanConfigOverrides> modelConfig,
            Optional<BedPlanOutput> lastModelOutput) {
        Map<String, Department> departmentMap = new LinkedHashMap<>();
        Map<String, Room> roomMap = new LinkedHashMap<>();
        Map<String, Bed> bedMap = new LinkedHashMap<>();

        for (DepartmentDTO departmentDto : modelInput.departments()) {
            Department department = new Department(departmentDto.id(), departmentDto.name(),
                    departmentDto.minimumAge(), departmentDto.maximumAge(),
                    new HashMap<>(departmentDto.specialtyToPriority()), new ArrayList<>());
            departmentMap.put(department.id(), department);

            for (RoomDTO roomDto : departmentDto.rooms()) {
                Room room = new Room(roomDto.id(), roomDto.name(), department, roomDto.capacity(),
                        GenderLimitation.valueOf(roomDto.genderLimitation()), Set.copyOf(roomDto.equipments()),
                        new ArrayList<>());
                department.rooms().add(room);
                roomMap.put(room.id(), room);

                for (BedDTO bedDto : roomDto.beds()) {
                    Bed bed = new Bed(bedDto.id(), room, bedDto.indexInRoom());
                    room.beds().add(bed);
                    bedMap.put(bed.id(), bed);
                }
            }
        }

        List<Stay> stays = modelInput.stays().stream()
                .map(dto -> toStay(dto, bedMap))
                .collect(Collectors.toCollection(ArrayList::new));

        BedPlan bedPlan = new BedPlan(new ArrayList<>(departmentMap.values()), new ArrayList<>(roomMap.values()),
                new ArrayList<>(bedMap.values()), stays);
        applyConstraintWeightOverrides(bedPlan, modelConfig);
        applyLastOutput(stays, bedMap, lastModelOutput);
        return bedPlan;
    }

    private static Stay toStay(StayDTO dto, Map<String, Bed> bedMap) {
        Bed bed = dto.bedId() == null ? null : require(bedMap, dto.bedId(), "bed");
        return new Stay(dto.id(), dto.patientName(), Gender.valueOf(dto.patientGender()), dto.patientAge(),
                dto.patientPreferredMaximumRoomCapacity(), new ArrayList<>(dto.patientRequiredEquipments()),
                new ArrayList<>(dto.patientPreferredEquipments()), LocalDate.parse(dto.arrivalDate()),
                LocalDate.parse(dto.departureDate()), dto.specialty(), bed);
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
        BedPlanConfigOverrides overrides = modelConfig.overrides();
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
        Map<String, Stay> stayMap = stays.stream().collect(Collectors.toMap(Stay::getId, stay -> stay));
        for (StayDTO solved : lastModelOutput.get().stays()) {
            Stay stay = stayMap.get(solved.id());
            if (stay == null || solved.bedId() == null) {
                continue;
            }
            Bed bed = bedMap.get(solved.bedId());
            if (bed != null) {
                stay.setBed(bed);
            }
        }
    }

    @Override
    public BedPlanOutput toModelOutput(BedPlan solverModel) {
        List<DepartmentDTO> departments = solverModel.getDepartments().stream().map(this::toDTO).collect(Collectors.toList());
        List<StayDTO> stays = solverModel.getStays().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new BedPlanOutput(departments, stays, score);
    }

    private DepartmentDTO toDTO(Department department) {
        List<RoomDTO> rooms = department.rooms().stream().map(this::toDTO).toList();
        return new DepartmentDTO(department.id(), department.name(), department.minimumAge(), department.maximumAge(),
                Map.copyOf(department.specialtyToPriority()), rooms);
    }

    private RoomDTO toDTO(Room room) {
        List<BedDTO> beds = room.beds().stream().map(this::toDTO).toList();
        return new RoomDTO(room.id(), room.name(), room.capacity(), room.genderLimitation().name(),
                Set.copyOf(room.equipments()), beds);
    }

    private BedDTO toDTO(Bed bed) {
        return new BedDTO(bed.id(), bed.indexInRoom());
    }

    private StayDTO toDTO(Stay stay) {
        String bedId = stay.getBed() == null ? null : stay.getBed().id();
        return new StayDTO(stay.getId(), stay.getPatientName(), stay.getPatientGender().name(), stay.getPatientAge(),
                stay.getPatientPreferredMaximumRoomCapacity(), List.copyOf(stay.getPatientRequiredEquipments()),
                List.copyOf(stay.getPatientPreferredEquipments()), stay.getArrivalDate().toString(),
                stay.getDepartureDate().toString(), stay.getSpecialty(), bedId);
    }
}
