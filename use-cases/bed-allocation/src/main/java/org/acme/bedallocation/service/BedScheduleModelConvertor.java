package org.acme.bedallocation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedSchedule;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedScheduleConfigOverrides;
import org.acme.bedallocation.dto.BedScheduleInput;
import org.acme.bedallocation.dto.BedScheduleOutput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;
import org.acme.bedallocation.solver.BedScheduleConstraintProvider;

@ApplicationScoped
public class BedScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, BedScheduleInput, BedScheduleConfigOverrides, BedSchedule, BedScheduleOutput> {

    @Override
    public BedScheduleInput applyOutputToInput(BedScheduleInput modelInput, BedScheduleOutput modelOutput) {
        Map<String, StayDTO> outputStays = modelOutput.stays().stream()
                .collect(Collectors.toMap(StayDTO::id, stay -> stay));
        List<StayDTO> updatedStays = modelInput.stays().stream()
                .map(stay -> {
                    StayDTO solved = outputStays.get(stay.id());
                    if (solved == null) {
                        return stay;
                    }
                    return stay.withBedId(solved.bedId());
                })
                .collect(Collectors.toList());
        return new BedScheduleInput(modelInput.departments(), modelInput.rooms(), modelInput.beds(), updatedStays);
    }

    @Override
    public BedSchedule toSolverModel(BedScheduleInput modelInput,
            ModelConfig<BedScheduleConfigOverrides> modelConfig,
            Optional<BedScheduleOutput> lastModelOutput) {
        Map<String, Department> departmentMap = new HashMap<>();
        List<Department> departments = modelInput.departments().stream()
                .map(dto -> toDepartment(dto, departmentMap))
                .collect(Collectors.toList());

        Map<String, Room> roomMap = new HashMap<>();
        List<Room> rooms = modelInput.rooms().stream()
                .map(dto -> toRoom(dto, departmentMap, roomMap))
                .collect(Collectors.toList());

        Map<String, Bed> bedMap = new HashMap<>();
        List<Bed> beds = modelInput.beds().stream()
                .map(dto -> toBed(dto, roomMap, bedMap))
                .collect(Collectors.toList());

        List<Stay> stays = modelInput.stays().stream()
                .map(dto -> toStay(dto, bedMap))
                .collect(Collectors.toList());

        BedSchedule schedule = new BedSchedule(departments, rooms, beds, stays);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(stays, bedMap, lastModelOutput);
        return schedule;
    }

    private static Department toDepartment(DepartmentDTO dto, Map<String, Department> departmentMap) {
        Department department = new Department(dto.id(), dto.name());
        department.setMinimumAge(dto.minimumAge());
        department.setMaximumAge(dto.maximumAge());
        department.setSpecialtyToPriority(new HashMap<>(dto.specialtyToPriority()));
        departmentMap.put(department.getId(), department);
        return department;
    }

    private static Room toRoom(RoomDTO dto, Map<String, Department> departmentMap, Map<String, Room> roomMap) {
        Department department = dto.departmentId() == null ? null : departmentMap.get(dto.departmentId());
        Room room = department == null ? new Room(dto.id()) : new Room(dto.id(), dto.name(), department);
        room.setName(dto.name());
        room.setCapacity(dto.capacity());
        if (dto.genderLimitation() != null) {
            room.setGenderLimitation(GenderLimitation.valueOf(dto.genderLimitation()));
        }
        room.setEquipments(new ArrayList<>(dto.equipments()));
        roomMap.put(room.getId(), room);
        return room;
    }

    private static Bed toBed(BedDTO dto, Map<String, Room> roomMap, Map<String, Bed> bedMap) {
        Room room = dto.roomId() == null ? null : roomMap.get(dto.roomId());
        Bed bed = new Bed(dto.id(), room, dto.indexInRoom());
        if (room != null) {
            room.addBed(bed);
        }
        bedMap.put(bed.getId(), bed);
        return bed;
    }

    private static Stay toStay(StayDTO dto, Map<String, Bed> bedMap) {
        Stay stay = new Stay(dto.id(), dto.patientName());
        if (dto.patientGender() != null) {
            stay.setPatientGender(Gender.valueOf(dto.patientGender()));
        }
        stay.setPatientAge(dto.patientAge());
        stay.setPatientPreferredMaximumRoomCapacity(dto.patientPreferredMaximumRoomCapacity());
        stay.setPatientRequiredEquipments(new ArrayList<>(dto.patientRequiredEquipments()));
        stay.setPatientPreferredEquipments(new ArrayList<>(dto.patientPreferredEquipments()));
        stay.setArrivalDate(LocalDate.parse(dto.arrivalDate()));
        stay.setDepartureDate(LocalDate.parse(dto.departureDate()));
        stay.setSpecialty(dto.specialty());
        if (dto.bedId() != null) {
            stay.setBed(bedMap.get(dto.bedId()));
        }
        return stay;
    }

    private static void applyConstraintWeightOverrides(BedSchedule schedule,
            ModelConfig<BedScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        BedScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, BedScheduleConstraintProvider.PREFERRED_MAXIMUM_ROOM_CAPACITY,
                overrides.preferredMaximumRoomCapacityWeight());
        putIfPresent(weights, BedScheduleConstraintProvider.DEPARTMENT_SPECIALTY,
                overrides.departmentSpecialtyWeight());
        putIfPresent(weights, BedScheduleConstraintProvider.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY,
                overrides.departmentSpecialtyNotFirstPriorityWeight());
        putIfPresent(weights, BedScheduleConstraintProvider.PREFERRED_PATIENT_EQUIPMENT,
                overrides.preferredPatientEquipmentWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Stay> stays, Map<String, Bed> bedMap,
            Optional<BedScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, StayDTO> assignmentMap = lastModelOutput.get().stays().stream()
                .collect(Collectors.toMap(StayDTO::id, stay -> stay));
        for (Stay stay : stays) {
            StayDTO solved = assignmentMap.get(stay.getId());
            if (solved != null && solved.bedId() != null) {
                stay.setBed(bedMap.get(solved.bedId()));
            }
        }
    }

    @Override
    public BedScheduleOutput toModelOutput(BedSchedule solverModel) {
        List<DepartmentDTO> departments = solverModel.getDepartments().stream()
                .map(this::toDTO).collect(Collectors.toList());
        List<RoomDTO> rooms = solverModel.getRooms().stream().map(this::toDTO).collect(Collectors.toList());
        List<BedDTO> beds = solverModel.getBeds().stream().map(this::toDTO).collect(Collectors.toList());
        List<StayDTO> stays = solverModel.getStays().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new BedScheduleOutput(departments, rooms, beds, stays, score);
    }

    private DepartmentDTO toDTO(Department department) {
        return new DepartmentDTO(department.getId(), department.getName(), department.getMinimumAge(),
                department.getMaximumAge(), Map.copyOf(department.getSpecialtyToPriority()));
    }

    private RoomDTO toDTO(Room room) {
        String departmentId = room.getDepartment() == null ? null : room.getDepartment().getId();
        String genderLimitation = room.getGenderLimitation() == null ? null : room.getGenderLimitation().name();
        return new RoomDTO(room.getId(), room.getName(), departmentId, room.getCapacity(), genderLimitation,
                List.copyOf(room.getEquipments()));
    }

    private BedDTO toDTO(Bed bed) {
        String roomId = bed.getRoom() == null ? null : bed.getRoom().getId();
        return new BedDTO(bed.getId(), roomId, bed.getIndexInRoom());
    }

    private StayDTO toDTO(Stay stay) {
        String gender = stay.getPatientGender() == null ? null : stay.getPatientGender().name();
        String bedId = stay.getBed() == null ? null : stay.getBed().getId();
        return new StayDTO(stay.getId(), stay.getPatientName(), gender, stay.getPatientAge(),
                stay.getPatientPreferredMaximumRoomCapacity(),
                List.copyOf(stay.getPatientRequiredEquipments()),
                List.copyOf(stay.getPatientPreferredEquipments()),
                stay.getArrivalDate().toString(), stay.getDepartureDate().toString(),
                stay.getSpecialty(), bedId);
    }
}
