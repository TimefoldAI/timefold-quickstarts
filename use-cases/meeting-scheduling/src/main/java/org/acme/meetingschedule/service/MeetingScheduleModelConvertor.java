package org.acme.meetingschedule.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.meetingschedule.domain.Attendance;
import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.domain.MeetingScheduleConstraintProperties;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.PreferredAttendance;
import org.acme.meetingschedule.domain.RequiredAttendance;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;
import org.acme.meetingschedule.dto.input.MeetingInputDTO;
import org.acme.meetingschedule.dto.input.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;
import org.acme.meetingschedule.dto.input.TimeConfigurationDTO;
import org.acme.meetingschedule.dto.output.MeetingOutputDTO;
import org.acme.meetingschedule.dto.output.MeetingScheduleOutput;

@ApplicationScoped
public class MeetingScheduleModelConvertor implements
        ModelConvertor<HardMediumSoftScore, MeetingScheduleInput, MeetingScheduleConfigOverrides, MeetingSchedule, MeetingScheduleOutput> {

    @Override
    public MeetingSchedule toSolverModel(MeetingScheduleInput modelInput,
            ModelConfig<MeetingScheduleConfigOverrides> modelConfig, Optional<MeetingScheduleOutput> lastModelOutput) {
        Map<String, Person> personMap = modelInput.people().stream()
                .map(dto -> new Person(dto.id(), dto.fullName()))
                .collect(Collectors.toMap(Person::id, person -> person, (left, right) -> left, LinkedHashMap::new));
        Map<String, Room> roomMap = modelInput.rooms().stream()
                .map(dto -> new Room(dto.id(), dto.name(), dto.capacity()))
                .collect(Collectors.toMap(Room::id, room -> room, (left, right) -> left, LinkedHashMap::new));

        TimeConfigurationDTO timeConfiguration = modelInput.timeConfiguration();
        int lengthInMinutes = timeConfiguration.granularityInMinutes();
        List<TimeGrain> timeGrains = toTimeGrains(timeConfiguration);
        // Keyed by instant, so a meeting may state its start in a different offset than the office hours do.
        Map<Instant, TimeGrain> timeGrainByStart = timeGrains.stream()
                .collect(Collectors.toMap(timeGrain -> timeGrain.startDateTime().toInstant(),
                        timeGrain -> timeGrain, (left, right) -> left, LinkedHashMap::new));

        List<Meeting> meetings = new ArrayList<>();
        List<Attendance> attendances = new ArrayList<>();
        List<MeetingAssignment> meetingAssignments = new ArrayList<>();
        for (MeetingInputDTO dto : modelInput.meetings()) {
            int requiredCapacity = dto.requiredAttendeeIds().size() + dto.preferredAttendeeIds().size();
            var meeting = new Meeting(dto.id(), dto.topic(), toDurationInGrains(dto, lengthInMinutes), requiredCapacity);
            meetings.add(meeting);
            for (String personId : dto.requiredAttendeeIds()) {
                attendances.add(new RequiredAttendance("%s-required-%s".formatted(dto.id(), personId), meeting,
                        require(personMap, personId, "person")));
            }
            for (String personId : dto.preferredAttendeeIds()) {
                attendances.add(new PreferredAttendance("%s-preferred-%s".formatted(dto.id(), personId), meeting,
                        require(personMap, personId, "person")));
            }
            // One assignment per meeting; it shares the meeting's id, which is what the output reports back.
            meetingAssignments.add(new MeetingAssignment(dto.id(), meeting,
                    toTimeGrain(dto.startDateTime(), timeGrainByStart),
                    dto.roomId() == null ? null : require(roomMap, dto.roomId(), "room"),
                    Boolean.TRUE.equals(dto.pinned())));
        }

        var meetingSchedule = new MeetingSchedule(List.copyOf(personMap.values()), timeGrains,
                List.copyOf(roomMap.values()), meetings, attendances, meetingAssignments);
        applyConstraintWeightOverrides(meetingSchedule, modelConfig);
        applyLastOutput(meetingAssignments, roomMap, timeGrainByStart, lastModelOutput);
        return meetingSchedule;
    }

    /**
     * Divides the submitted office hours into the time grains the solver plans with. This is the only place that turns
     * the wire format's office hours and granularity into grains; everything the client sees is expressed in minutes
     * and date-times instead.
     */
    private static List<TimeGrain> toTimeGrains(TimeConfigurationDTO timeConfiguration) {
        List<OffsetDateTime> slotStartDateTimes = timeConfiguration.slotStartDateTimes();
        int lengthInMinutes = timeConfiguration.granularityInMinutes();
        List<TimeGrain> timeGrains = new ArrayList<>(slotStartDateTimes.size());
        for (int grainIndex = 0; grainIndex < slotStartDateTimes.size(); grainIndex++) {
            timeGrains.add(new TimeGrain("G%d".formatted(grainIndex), grainIndex, slotStartDateTimes.get(grainIndex),
                    lengthInMinutes));
        }
        return timeGrains;
    }

    private static int toDurationInGrains(MeetingInputDTO dto, int lengthInMinutes) {
        Integer durationInMinutes = dto.durationInMinutes();
        if (durationInMinutes == null || durationInMinutes <= 0 || durationInMinutes % lengthInMinutes != 0) {
            throw new IllegalArgumentException(
                    "The duration (%s minutes) of meeting '%s' must be a positive whole multiple of the granularity (%d minutes)."
                            .formatted(durationInMinutes, dto.id(), lengthInMinutes));
        }
        return durationInMinutes / lengthInMinutes;
    }

    private static TimeGrain toTimeGrain(OffsetDateTime startDateTime, Map<Instant, TimeGrain> timeGrainByStart) {
        return startDateTime == null ? null
                : require(timeGrainByStart, startDateTime.toInstant(), "meeting start");
    }

    @Override
    public MeetingScheduleOutput toModelOutput(MeetingSchedule solverModel) {
        var meetings = solverModel.getMeetingAssignments().stream()
                .map(assignment -> new MeetingOutputDTO(assignment.getId(),
                        assignment.getRoom() == null ? null : assignment.getRoom().id(),
                        assignment.getStartDateTime()))
                .toList();
        return new MeetingScheduleOutput(meetings);
    }

    @Override
    public MeetingScheduleInput applyOutputToInput(MeetingScheduleInput modelInput, MeetingScheduleOutput modelOutput) {
        Map<String, MeetingOutputDTO> outputMeetings = modelOutput.meetings().stream()
                .collect(Collectors.toMap(MeetingOutputDTO::id, meeting -> meeting));
        List<MeetingInputDTO> updatedMeetings = modelInput.meetings().stream()
                .map(meeting -> {
                    MeetingOutputDTO solved = outputMeetings.get(meeting.id());
                    return solved == null ? meeting : meeting.withAssignment(solved.roomId(), solved.startDateTime());
                })
                .toList();
        return modelInput.withMeetings(updatedMeetings);
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <K, T> T require(Map<K, T> map, K key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(MeetingSchedule meetingSchedule,
            ModelConfig<MeetingScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                overrides.doAllMeetingsAsSoonAsPossibleWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.ONE_BREAK_BETWEEN_CONSECUTIVE_MEETINGS,
                overrides.oneBreakBetweenConsecutiveMeetingsWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS,
                overrides.overlappingMeetingsWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST,
                overrides.assignLargerRoomsFirstWeight());
        putIfPresent(weights, MeetingScheduleConstraintProperties.ROOM_STABILITY, overrides.roomStabilityWeight());
        if (!weights.isEmpty()) {
            meetingSchedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway. It should override the input assignment.
    private static void applyLastOutput(List<MeetingAssignment> meetingAssignments, Map<String, Room> roomMap,
            Map<Instant, TimeGrain> timeGrainByStart, Optional<MeetingScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        var assignmentMap = meetingAssignments.stream()
                .collect(Collectors.toMap(MeetingAssignment::getId, assignment -> assignment));
        for (var solved : lastModelOutput.get().meetings()) {
            MeetingAssignment assignment = assignmentMap.get(solved.id());
            if (assignment == null) {
                continue;
            }
            Room room = roomMap.get(solved.roomId());
            if (room != null) {
                assignment.setRoom(room);
            }
            TimeGrain timeGrain = solved.startDateTime() == null ? null
                    : timeGrainByStart.get(solved.startDateTime().toInstant());
            if (timeGrain != null) {
                assignment.setStartingTimeGrain(timeGrain);
            }
        }
    }
}
