package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a conference schedule input.",
        oneOf = {
                OpenApiSpecIssue.class,
                ConferenceScheduleIssue.DuplicateTimeslotIdIssue.class,
                ConferenceScheduleIssue.DuplicateRoomIdIssue.class,
                ConferenceScheduleIssue.DuplicateSpeakerIdIssue.class,
                ConferenceScheduleIssue.DuplicateTalkIdIssue.class,
                ConferenceScheduleIssue.NonExistingTimeslotReferenceIssue.class,
                ConferenceScheduleIssue.NonExistingRoomReferenceIssue.class,
                ConferenceScheduleIssue.NonExistingSpeakerReferenceIssue.class,
                ConferenceScheduleIssue.NonExistingTalkTypeReferenceIssue.class
        })
public abstract class ConferenceScheduleIssue extends AbstractIssue {

    protected ConferenceScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class DuplicateTimeslotIdIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TIMESLOT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate timeslot ID found.");

        @Schema(description = "The ID of the duplicated timeslot.")
        private String timeslotId;

        public DuplicateTimeslotIdIssue() {
            this(null);
        }

        public DuplicateTimeslotIdIssue(String timeslotId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.timeslotId = timeslotId;
        }

        public String getTimeslotId() {
            return timeslotId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class DuplicateRoomIdIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_ROOM_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate room ID found.");

        @Schema(description = "The ID of the duplicated room.")
        private String roomId;

        public DuplicateRoomIdIssue() {
            this(null);
        }

        public DuplicateRoomIdIssue(String roomId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.roomId = roomId;
        }

        public String getRoomId() {
            return roomId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class DuplicateSpeakerIdIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_SPEAKER_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate speaker ID found.");

        @Schema(description = "The ID of the duplicated speaker.")
        private String speakerId;

        public DuplicateSpeakerIdIssue() {
            this(null);
        }

        public DuplicateSpeakerIdIssue(String speakerId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.speakerId = speakerId;
        }

        public String getSpeakerId() {
            return speakerId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class DuplicateTalkIdIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TALK_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate talk code found.");

        @Schema(description = "The code of the duplicated talk.")
        private String talkId;

        public DuplicateTalkIdIssue() {
            this(null);
        }

        public DuplicateTalkIdIssue(String talkId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.talkId = talkId;
        }

        public String getTalkId() {
            return talkId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class NonExistingTimeslotReferenceIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TIMESLOT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Talk references non-existing timeslot.");

        @Schema(description = "The code of the talk with the dangling reference, if it has one.")
        private String talkId;

        public NonExistingTimeslotReferenceIssue() {
            this(null);
        }

        public NonExistingTimeslotReferenceIssue(String talkId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.talkId = talkId;
        }

        public String getTalkId() {
            return talkId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class NonExistingRoomReferenceIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_ROOM_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Talk references non-existing room.");

        @Schema(description = "The code of the talk with the dangling reference, if it has one.")
        private String talkId;

        public NonExistingRoomReferenceIssue() {
            this(null);
        }

        public NonExistingRoomReferenceIssue(String talkId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.talkId = talkId;
        }

        public String getTalkId() {
            return talkId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class NonExistingSpeakerReferenceIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_SPEAKER_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Talk references non-existing speaker.");

        @Schema(description = "The code of the talk with the dangling reference, if it has one.")
        private String talkId;

        public NonExistingSpeakerReferenceIssue() {
            this(null);
        }

        public NonExistingSpeakerReferenceIssue(String talkId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.talkId = talkId;
        }

        public String getTalkId() {
            return talkId;
        }
    }

    @Schema(allOf = { ConferenceScheduleIssue.class })
    public static class NonExistingTalkTypeReferenceIssue extends ConferenceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TALK_TYPE_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Talk references non-existing talk type.");

        @Schema(description = "The code of the talk with the dangling reference, if it has one.")
        private String talkId;

        public NonExistingTalkTypeReferenceIssue() {
            this(null);
        }

        public NonExistingTalkTypeReferenceIssue(String talkId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.talkId = talkId;
        }

        public String getTalkId() {
            return talkId;
        }
    }
}
