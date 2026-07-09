package org.acme.meetingschedule.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.meetingschedule.dto.MeetingAssignmentIdDetail;
import org.acme.meetingschedule.dto.MeetingIdDetail;
import org.acme.meetingschedule.dto.MeetingScheduleValidationIssue;
import org.acme.meetingschedule.dto.PersonIdDetail;
import org.acme.meetingschedule.dto.RoomIdDetail;
import org.acme.meetingschedule.dto.TimeGrainIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class MeetingScheduleIssues {

    private MeetingScheduleIssues() {
    }

    public abstract static class MeetingScheduleIssue extends AbstractIssue {
        protected MeetingScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class MeetingIdMissingIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.MEETING_ID_MISSING.asIssueType();

        public MeetingIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateMeetingIdIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.DUPLICATE_MEETING_ID.asIssueType();

        public DuplicateMeetingIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateMeetingIdIssue(MeetingIdDetail meetingIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(meetingIdDetail)).toList());
        }
    }

    public static final class RoomIdMissingIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.ROOM_ID_MISSING.asIssueType();

        public RoomIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateRoomIdIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.DUPLICATE_ROOM_ID.asIssueType();

        public DuplicateRoomIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateRoomIdIssue(RoomIdDetail roomIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(roomIdDetail)).toList());
        }
    }

    public static final class TimeGrainIdMissingIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.TIME_GRAIN_ID_MISSING.asIssueType();

        public TimeGrainIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTimeGrainIdIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.DUPLICATE_TIME_GRAIN_ID.asIssueType();

        public DuplicateTimeGrainIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTimeGrainIdIssue(TimeGrainIdDetail timeGrainIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(timeGrainIdDetail)).toList());
        }
    }

    public static final class PersonIdMissingIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.PERSON_ID_MISSING.asIssueType();

        public PersonIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicatePersonIdIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.DUPLICATE_PERSON_ID.asIssueType();

        public DuplicatePersonIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicatePersonIdIssue(PersonIdDetail personIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(personIdDetail)).toList());
        }
    }

    public static final class MeetingAssignmentIdMissingIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.MEETING_ASSIGNMENT_ID_MISSING.asIssueType();

        public MeetingAssignmentIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateMeetingAssignmentIdIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.DUPLICATE_MEETING_ASSIGNMENT_ID.asIssueType();

        public DuplicateMeetingAssignmentIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateMeetingAssignmentIdIssue(MeetingAssignmentIdDetail meetingAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(meetingAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingMeetingReferenceIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.NON_EXISTING_MEETING_REFERENCE.asIssueType();

        public NonExistingMeetingReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingMeetingReferenceIssue(MeetingAssignmentIdDetail meetingAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(meetingAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingTimeGrainReferenceIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.NON_EXISTING_TIME_GRAIN_REFERENCE.asIssueType();

        public NonExistingTimeGrainReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTimeGrainReferenceIssue(MeetingAssignmentIdDetail meetingAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(meetingAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingRoomReferenceIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE = MeetingScheduleValidationIssue.NON_EXISTING_ROOM_REFERENCE.asIssueType();

        public NonExistingRoomReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingRoomReferenceIssue(MeetingAssignmentIdDetail meetingAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(meetingAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingAttendancePersonReferenceIssue extends MeetingScheduleIssue {
        private static final IssueType TYPE =
                MeetingScheduleValidationIssue.NON_EXISTING_ATTENDANCE_PERSON_REFERENCE.asIssueType();

        public NonExistingAttendancePersonReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingAttendancePersonReferenceIssue(MeetingIdDetail meetingIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(meetingIdDetail)).toList());
        }
    }
}
