package org.acme.schooltimetabling.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.schooltimetabling.dto.LessonIdDetail;
import org.acme.schooltimetabling.dto.RoomIdDetail;
import org.acme.schooltimetabling.dto.TimeslotIdDetail;
import org.acme.schooltimetabling.dto.TimetableValidationIssue;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class TimetableIssues {

    private TimetableIssues() {
    }

    public abstract static class TimetableIssue extends AbstractIssue {
        protected TimetableIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class LessonIdMissingIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.LESSON_ID_MISSING.asIssueType();

        public LessonIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateLessonIdIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.DUPLICATE_LESSON_ID.asIssueType();

        public DuplicateLessonIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateLessonIdIssue(LessonIdDetail lessonIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(lessonIdDetail)).toList());
        }
    }

    public static final class TimeslotIdMissingIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.TIMESLOT_ID_MISSING.asIssueType();

        public TimeslotIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTimeslotIdIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.DUPLICATE_TIMESLOT_ID.asIssueType();

        public DuplicateTimeslotIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTimeslotIdIssue(TimeslotIdDetail timeslotIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(timeslotIdDetail)).toList());
        }
    }

    public static final class RoomIdMissingIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.ROOM_ID_MISSING.asIssueType();

        public RoomIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateRoomIdIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.DUPLICATE_ROOM_ID.asIssueType();

        public DuplicateRoomIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateRoomIdIssue(RoomIdDetail roomIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(roomIdDetail)).toList());
        }
    }

    public static final class NonExistingTimeslotReferenceIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.NON_EXISTING_TIMESLOT_REFERENCE.asIssueType();

        public NonExistingTimeslotReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTimeslotReferenceIssue(LessonIdDetail lessonIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(lessonIdDetail)).toList());
        }
    }

    public static final class NonExistingRoomReferenceIssue extends TimetableIssue {
        private static final IssueType TYPE = TimetableValidationIssue.NON_EXISTING_ROOM_REFERENCE.asIssueType();

        public NonExistingRoomReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingRoomReferenceIssue(LessonIdDetail lessonIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(lessonIdDetail)).toList());
        }
    }
}
