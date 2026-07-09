package org.acme.meetingschedule.service;

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

import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.acme.meetingschedule.dto.MeetingAssignmentDTO;
import org.acme.meetingschedule.dto.MeetingDTO;
import org.acme.meetingschedule.dto.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.MeetingScheduleInput;
import org.acme.meetingschedule.dto.MeetingScheduleOutput;
import org.acme.meetingschedule.dto.PersonDTO;
import org.acme.meetingschedule.dto.RoomDTO;
import org.acme.meetingschedule.dto.TimeGrainDTO;
import org.acme.meetingschedule.solver.MeetingScheduleConstraintProperties;

@ApplicationScoped
public class MeetingScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, MeetingScheduleInput, MeetingScheduleConfigOverrides, MeetingSchedule, MeetingScheduleOutput> {

    @Override
    public MeetingScheduleInput applyOutputToInput(MeetingScheduleInput modelInput, MeetingScheduleOutput modelOutput) {
        Map<String, MeetingAssignmentDTO> outputAssignments = modelOutput.meetingAssignments().stream()
                .collect(Collectors.toMap(MeetingAssignmentDTO::id, assignment -> assignment));
        List<MeetingAssignmentDTO> updatedAssignments = modelInput.meetingAssignments().stream()
                .map(assignment -> {
                    MeetingAssignmentDTO solved = outputAssignments.get(assignment.id());
                    if (solved == null) {
                        return assignment;
                    }
                    return assignment.withStartingTimeGrainId(solved.startingTimeGrainId()).withRoomId(solved.roomId());
                })
                .collect(Collectors.toList());
        return new MeetingScheduleInput(modelInput.people(), modelInput.timeGrains(), modelInput.rooms(),
                modelInput.meetings(), updatedAssignments);
    }

    @Override
    public MeetingSchedule toSolverModel(MeetingScheduleInput modelInput,
            ModelConfig<MeetingScheduleConfigOverrides> modelConfig, Optional<MeetingScheduleOutput> lastModelOutput) {
        Map<String, Person> personMap = new HashMap<>();
        List<Person> people = modelInput.people().stream().map(dto -> {
            Person person = new Person(dto.id(), dto.fullName());
            personMap.put(person.getId(), person);
            return person;
        }).collect(Collectors.toList());

        Map<String, TimeGrain> timeGrainMap = new HashMap<>();
        List<TimeGrain> timeGrains = modelInput.timeGrains().stream().map(dto -> {
            TimeGrain timeGrain =
                    new TimeGrain(dto.id(), dto.grainIndex(), dto.dayOfYear(), dto.startingMinuteOfDay());
            timeGrainMap.put(timeGrain.getId(), timeGrain);
            return timeGrain;
        }).collect(Collectors.toList());

        Map<String, Room> roomMap = new HashMap<>();
        List<Room> rooms = modelInput.rooms().stream().map(dto -> {
            Room room = new Room(dto.id(), dto.name(), dto.capacity());
            roomMap.put(room.getId(), room);
            return room;
        }).collect(Collectors.toList());

        Map<String, Meeting> meetingMap = new HashMap<>();
        List<Meeting> meetings = modelInput.meetings().stream()
                .map(dto -> toMeeting(dto, personMap, meetingMap))
                .collect(Collectors.toList());

        List<MeetingAssignment> meetingAssignments = modelInput.meetingAssignments().stream().map(dto -> {
            MeetingAssignment assignment = new MeetingAssignment(dto.id(), meetingMap.get(dto.meetingId()));
            assignment.setPinned(dto.pinned());
            if (dto.startingTimeGrainId() != null) {
                assignment.setStartingTimeGrain(timeGrainMap.get(dto.startingTimeGrainId()));
            }
            if (dto.roomId() != null) {
                assignment.setRoom(roomMap.get(dto.roomId()));
            }
            return assignment;
        }).collect(Collectors.toList());

        MeetingSchedule schedule = new MeetingSchedule(people, timeGrains, rooms, meetings, meetingAssignments);
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(meetingAssignments, timeGrainMap, roomMap, lastModelOutput);
        return schedule;
    }

    private static Meeting toMeeting(MeetingDTO dto, Map<String, Person> personMap, Map<String, Meeting> meetingMap) {
        Meeting meeting = new Meeting(dto.id(), dto.topic(), dto.durationInGrains());
        for (String personId : dto.requiredAttendancePersonIds()) {
            Person person = personMap.get(personId);
            if (person != null) {
                meeting.addRequiredAttendant(person);
            }
        }
        for (String personId : dto.preferredAttendancePersonIds()) {
            Person person = personMap.get(personId);
            if (person != null) {
                meeting.addPreferredAttendant(person);
            }
        }
        meetingMap.put(meeting.getId(), meeting);
        return meeting;
    }

    private static void applyConstraintWeightOverrides(MeetingSchedule schedule,
            ModelConfig<MeetingScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        MeetingScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                overrides.doMeetingsAsSoonAsPossibleWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.ONE_TIME_GRAIN_BREAK_BETWEEN_TWO_CONSECUTIVE_MEETINGS,
                overrides.oneBreakBetweenConsecutiveMeetingsWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS,
                overrides.overlappingMeetingsWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST,
                overrides.assignLargerRoomsFirstWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.ROOM_STABILITY,
                overrides.roomStabilityWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<MeetingAssignment> assignments, Map<String, TimeGrain> timeGrainMap,
            Map<String, Room> roomMap, Optional<MeetingScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, MeetingAssignmentDTO> assignmentMap = lastModelOutput.get().meetingAssignments().stream()
                .collect(Collectors.toMap(MeetingAssignmentDTO::id, assignment -> assignment));
        for (MeetingAssignment assignment : assignments) {
            MeetingAssignmentDTO solved = assignmentMap.get(assignment.getId());
            if (solved == null) {
                continue;
            }
            if (solved.startingTimeGrainId() != null) {
                assignment.setStartingTimeGrain(timeGrainMap.get(solved.startingTimeGrainId()));
            }
            if (solved.roomId() != null) {
                assignment.setRoom(roomMap.get(solved.roomId()));
            }
        }
    }

    @Override
    public MeetingScheduleOutput toModelOutput(MeetingSchedule solverModel) {
        List<PersonDTO> people = solverModel.getPeople().stream().map(this::toDTO).collect(Collectors.toList());
        List<TimeGrainDTO> timeGrains = solverModel.getTimeGrains().stream().map(this::toDTO).collect(Collectors.toList());
        List<RoomDTO> rooms = solverModel.getRooms().stream().map(this::toDTO).collect(Collectors.toList());
        List<MeetingDTO> meetings = solverModel.getMeetings().stream().map(this::toDTO).collect(Collectors.toList());
        List<MeetingAssignmentDTO> meetingAssignments =
                solverModel.getMeetingAssignments().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }

    private PersonDTO toDTO(Person person) {
        return new PersonDTO(person.getId(), person.getFullName());
    }

    private TimeGrainDTO toDTO(TimeGrain timeGrain) {
        return new TimeGrainDTO(timeGrain.getId(), timeGrain.getGrainIndex(), timeGrain.getDayOfYear(),
                timeGrain.getStartingMinuteOfDay());
    }

    private RoomDTO toDTO(Room room) {
        return new RoomDTO(room.getId(), room.getName(), room.getCapacity());
    }

    private MeetingDTO toDTO(Meeting meeting) {
        List<String> requiredPersonIds = meeting.getRequiredAttendances().stream()
                .map(attendance -> attendance.getPerson().getId())
                .collect(Collectors.toList());
        List<String> preferredPersonIds = meeting.getPreferredAttendances().stream()
                .map(attendance -> attendance.getPerson().getId())
                .collect(Collectors.toList());
        return new MeetingDTO(meeting.getId(), meeting.getTopic(), meeting.getDurationInGrains(), requiredPersonIds,
                preferredPersonIds);
    }

    private MeetingAssignmentDTO toDTO(MeetingAssignment assignment) {
        String timeGrainId = assignment.getStartingTimeGrain() == null ? null : assignment.getStartingTimeGrain().getId();
        String roomId = assignment.getRoom() == null ? null : assignment.getRoom().getId();
        String meetingId = assignment.getMeeting() == null ? null : assignment.getMeeting().getId();
        return new MeetingAssignmentDTO(assignment.getId(), meetingId, timeGrainId, roomId, assignment.isPinned());
    }
}
