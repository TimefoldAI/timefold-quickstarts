package org.acme.conferencescheduling.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.conferencescheduling.dto.ConferenceScheduleValidationIssue;
import org.acme.conferencescheduling.dto.RoomIdDetail;
import org.acme.conferencescheduling.dto.SpeakerIdDetail;
import org.acme.conferencescheduling.dto.TalkIdDetail;
import org.acme.conferencescheduling.dto.TimeslotIdDetail;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class ConferenceScheduleIssues {

    private ConferenceScheduleIssues() {
    }

    @Schema(description = "A dataset validation issue reported for a conference schedule input.")
    public abstract static class ConferenceScheduleIssue extends AbstractIssue {
        protected ConferenceScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class TalkIdMissingIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.TALK_ID_MISSING.asIssueType();

        public TalkIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTalkIdIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.DUPLICATE_TALK_ID.asIssueType();

        public DuplicateTalkIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTalkIdIssue(TalkIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class SpeakerIdMissingIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.SPEAKER_ID_MISSING.asIssueType();

        public SpeakerIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateSpeakerIdIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.DUPLICATE_SPEAKER_ID.asIssueType();

        public DuplicateSpeakerIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateSpeakerIdIssue(SpeakerIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class RoomIdMissingIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.ROOM_ID_MISSING.asIssueType();

        public RoomIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateRoomIdIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.DUPLICATE_ROOM_ID.asIssueType();

        public DuplicateRoomIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateRoomIdIssue(RoomIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class TimeslotIdMissingIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.TIMESLOT_ID_MISSING.asIssueType();

        public TimeslotIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTimeslotIdIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE = ConferenceScheduleValidationIssue.DUPLICATE_TIMESLOT_ID.asIssueType();

        public DuplicateTimeslotIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTimeslotIdIssue(TimeslotIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class NonExistingTimeslotReferenceIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE =
                ConferenceScheduleValidationIssue.NON_EXISTING_TIMESLOT_REFERENCE.asIssueType();

        public NonExistingTimeslotReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTimeslotReferenceIssue(TalkIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class NonExistingRoomReferenceIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE =
                ConferenceScheduleValidationIssue.NON_EXISTING_ROOM_REFERENCE.asIssueType();

        public NonExistingRoomReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingRoomReferenceIssue(TalkIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class NonExistingSpeakerReferenceIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE =
                ConferenceScheduleValidationIssue.NON_EXISTING_SPEAKER_REFERENCE.asIssueType();

        public NonExistingSpeakerReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingSpeakerReferenceIssue(TalkIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class NonExistingTalkTypeReferenceIssue extends ConferenceScheduleIssue {
        private static final IssueType TYPE =
                ConferenceScheduleValidationIssue.NON_EXISTING_TALK_TYPE_REFERENCE.asIssueType();

        public NonExistingTalkTypeReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTalkTypeReferenceIssue(TalkIdDetail detail) {
            super(TYPE.code(), TYPE.severity(), Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }
}
