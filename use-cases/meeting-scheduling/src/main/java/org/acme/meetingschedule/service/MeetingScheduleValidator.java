package org.acme.meetingschedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.meetingschedule.dto.MeetingAssignmentDTO;
import org.acme.meetingschedule.dto.MeetingAssignmentIdDetail;
import org.acme.meetingschedule.dto.MeetingDTO;
import org.acme.meetingschedule.dto.MeetingIdDetail;
import org.acme.meetingschedule.dto.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.MeetingScheduleInput;
import org.acme.meetingschedule.dto.PersonDTO;
import org.acme.meetingschedule.dto.PersonIdDetail;
import org.acme.meetingschedule.dto.RoomDTO;
import org.acme.meetingschedule.dto.RoomIdDetail;
import org.acme.meetingschedule.dto.TimeGrainDTO;
import org.acme.meetingschedule.dto.TimeGrainIdDetail;
import org.acme.meetingschedule.service.MeetingScheduleIssues.DuplicateMeetingAssignmentIdIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.DuplicateMeetingIdIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.DuplicatePersonIdIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.DuplicateRoomIdIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.DuplicateTimeGrainIdIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.MeetingAssignmentIdMissingIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.MeetingIdMissingIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.NonExistingAttendancePersonReferenceIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.NonExistingMeetingReferenceIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.NonExistingRoomReferenceIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.NonExistingTimeGrainReferenceIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.PersonIdMissingIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.RoomIdMissingIssue;
import org.acme.meetingschedule.service.MeetingScheduleIssues.TimeGrainIdMissingIssue;

@ApplicationScoped
public class MeetingScheduleValidator implements ModelValidator<MeetingScheduleInput, MeetingScheduleConfigOverrides> {

    private static final String SUPPRESS_LOOP_INSTANTIATION = "PMD.AvoidInstantiatingObjectsInLoops";

    @Override
    public void validate(ValidationBuilder validationBuilder, MeetingScheduleInput modelInput,
            ModelConfig<MeetingScheduleConfigOverrides> modelConfig) {
        Set<String> personIds = validatePeople(validationBuilder, modelInput.people());
        Set<String> roomIds = validateRooms(validationBuilder, modelInput.rooms());
        Set<String> timeGrainIds = validateTimeGrains(validationBuilder, modelInput.timeGrains());
        Set<String> meetingIds = validateMeetings(validationBuilder, modelInput.meetings(), personIds);
        validateMeetingAssignments(validationBuilder, modelInput.meetingAssignments(), meetingIds, timeGrainIds, roomIds);
    }

    @SuppressWarnings(SUPPRESS_LOOP_INSTANTIATION)
    private Set<String> validatePeople(ValidationBuilder validationBuilder, List<PersonDTO> people) {
        Set<String> personIds = new HashSet<>();
        for (PersonDTO person : people) {
            if (person.id() == null || person.id().isBlank()) {
                validationBuilder.addIssue(new PersonIdMissingIssue());
            } else if (!personIds.add(person.id())) {
                validationBuilder.addIssue(new DuplicatePersonIdIssue(new PersonIdDetail(person.id())));
            }
        }
        return personIds;
    }

    @SuppressWarnings(SUPPRESS_LOOP_INSTANTIATION)
    private Set<String> validateRooms(ValidationBuilder validationBuilder, List<RoomDTO> rooms) {
        Set<String> roomIds = new HashSet<>();
        for (RoomDTO room : rooms) {
            if (room.id() == null || room.id().isBlank()) {
                validationBuilder.addIssue(new RoomIdMissingIssue());
            } else if (!roomIds.add(room.id())) {
                validationBuilder.addIssue(new DuplicateRoomIdIssue(new RoomIdDetail(room.id())));
            }
        }
        return roomIds;
    }

    @SuppressWarnings(SUPPRESS_LOOP_INSTANTIATION)
    private Set<String> validateTimeGrains(ValidationBuilder validationBuilder, List<TimeGrainDTO> timeGrains) {
        Set<String> timeGrainIds = new HashSet<>();
        for (TimeGrainDTO timeGrain : timeGrains) {
            if (timeGrain.id() == null || timeGrain.id().isBlank()) {
                validationBuilder.addIssue(new TimeGrainIdMissingIssue());
            } else if (!timeGrainIds.add(timeGrain.id())) {
                validationBuilder.addIssue(new DuplicateTimeGrainIdIssue(new TimeGrainIdDetail(timeGrain.id())));
            }
        }
        return timeGrainIds;
    }

    @SuppressWarnings(SUPPRESS_LOOP_INSTANTIATION)
    private Set<String> validateMeetings(ValidationBuilder validationBuilder, List<MeetingDTO> meetings,
            Set<String> personIds) {
        Set<String> meetingIds = new HashSet<>();
        for (MeetingDTO meeting : meetings) {
            if (meeting.id() == null || meeting.id().isBlank()) {
                validationBuilder.addIssue(new MeetingIdMissingIssue());
            } else if (!meetingIds.add(meeting.id())) {
                validationBuilder.addIssue(new DuplicateMeetingIdIssue(new MeetingIdDetail(meeting.id())));
            }
            if (hasUnknownPerson(meeting, personIds)) {
                validationBuilder.addIssue(new NonExistingAttendancePersonReferenceIssue(new MeetingIdDetail(meeting.id())));
            }
        }
        return meetingIds;
    }

    private static boolean hasUnknownPerson(MeetingDTO meeting, Set<String> personIds) {
        return meeting.requiredAttendancePersonIds().stream().anyMatch(personId -> !personIds.contains(personId))
                || meeting.preferredAttendancePersonIds().stream().anyMatch(personId -> !personIds.contains(personId));
    }

    @SuppressWarnings(SUPPRESS_LOOP_INSTANTIATION)
    private void validateMeetingAssignments(ValidationBuilder validationBuilder, List<MeetingAssignmentDTO> assignments,
            Set<String> meetingIds, Set<String> timeGrainIds, Set<String> roomIds) {
        Set<String> assignmentIds = new HashSet<>();
        for (MeetingAssignmentDTO assignment : assignments) {
            if (assignment.id() == null || assignment.id().isBlank()) {
                validationBuilder.addIssue(new MeetingAssignmentIdMissingIssue());
            } else if (!assignmentIds.add(assignment.id())) {
                validationBuilder
                        .addIssue(new DuplicateMeetingAssignmentIdIssue(new MeetingAssignmentIdDetail(assignment.id())));
            }
            if (assignment.meetingId() == null || !meetingIds.contains(assignment.meetingId())) {
                validationBuilder
                        .addIssue(new NonExistingMeetingReferenceIssue(new MeetingAssignmentIdDetail(assignment.id())));
            }
            if (assignment.startingTimeGrainId() != null && !timeGrainIds.contains(assignment.startingTimeGrainId())) {
                validationBuilder
                        .addIssue(new NonExistingTimeGrainReferenceIssue(new MeetingAssignmentIdDetail(assignment.id())));
            }
            if (assignment.roomId() != null && !roomIds.contains(assignment.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(new MeetingAssignmentIdDetail(assignment.id())));
            }
        }
    }
}
