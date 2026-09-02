package org.acme.meetingschedule.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a meeting scheduling input.",
        oneOf = {
                MeetingScheduleIssue.DuplicatePersonIdIssue.class,
                MeetingScheduleIssue.DuplicateRoomIdIssue.class,
                MeetingScheduleIssue.DuplicateMeetingIdIssue.class,
                MeetingScheduleIssue.InvalidOfficeHoursIssue.class,
                MeetingScheduleIssue.NonExistingPersonReferenceIssue.class,
                MeetingScheduleIssue.NonExistingRoomReferenceIssue.class,
                MeetingScheduleIssue.MeetingStartOutsideOfficeHoursIssue.class,
                MeetingScheduleIssue.MeetingDurationNotAMultipleOfGranularityIssue.class,
                MeetingScheduleIssue.MeetingLongerThanOfficeDayIssue.class
        })
public abstract class MeetingScheduleIssue extends AbstractIssue {

    protected MeetingScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class DuplicatePersonIdIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_PERSON_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate person ID found.");

        @Schema(description = "The ID of the duplicated person.")
        private String personId;

        public DuplicatePersonIdIssue() {
            this(null);
        }

        public DuplicatePersonIdIssue(String personId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.personId = personId;
        }

        public String getPersonId() {
            return personId;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class DuplicateRoomIdIssue extends MeetingScheduleIssue {

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

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class InvalidOfficeHoursIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INVALID_OFFICE_HOURS");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "Office hours end before they start, or are too short to hold a single slot of the configured granularity.");

        @Schema(description = "Start of the office hours that are invalid, in ISO-8601 date and time format with an offset.")
        private String startDateTime;

        public InvalidOfficeHoursIssue() {
            this(null);
        }

        public InvalidOfficeHoursIssue(String startDateTime) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.startDateTime = startDateTime;
        }

        public String getStartDateTime() {
            return startDateTime;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class DuplicateMeetingIdIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_MEETING_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate meeting ID found.");

        @Schema(description = "The ID of the duplicated meeting.")
        private String meetingId;

        public DuplicateMeetingIdIssue() {
            this(null);
        }

        public DuplicateMeetingIdIssue(String meetingId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.meetingId = meetingId;
        }

        public String getMeetingId() {
            return meetingId;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class NonExistingPersonReferenceIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_PERSON_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Meeting refers to a person ID that does not exist.");

        @Schema(description = "The ID of the meeting with the unknown attendee reference.")
        private String meetingId;

        public NonExistingPersonReferenceIssue() {
            this(null);
        }

        public NonExistingPersonReferenceIssue(String meetingId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.meetingId = meetingId;
        }

        public String getMeetingId() {
            return meetingId;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class NonExistingRoomReferenceIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_ROOM_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Meeting refers to a room ID that does not exist.");

        @Schema(description = "The ID of the meeting with the unknown room reference.")
        private String meetingId;

        public NonExistingRoomReferenceIssue() {
            this(null);
        }

        public NonExistingRoomReferenceIssue(String meetingId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.meetingId = meetingId;
        }

        public String getMeetingId() {
            return meetingId;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class MeetingStartOutsideOfficeHoursIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("MEETING_START_OUTSIDE_OFFICE_HOURS");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "Meeting starts at a moment that is not the start of a slot of the configured office hours.");

        @Schema(description = "The ID of the meeting with the unusable start.")
        private String meetingId;

        public MeetingStartOutsideOfficeHoursIssue() {
            this(null);
        }

        public MeetingStartOutsideOfficeHoursIssue(String meetingId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.meetingId = meetingId;
        }

        public String getMeetingId() {
            return meetingId;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class MeetingDurationNotAMultipleOfGranularityIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("MEETING_DURATION_NOT_A_MULTIPLE_OF_GRANULARITY");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "Meeting duration is not a whole number of the slots the office hours are divided into.");

        @Schema(description = "The ID of the meeting with the unusable duration.")
        private String meetingId;

        public MeetingDurationNotAMultipleOfGranularityIssue() {
            this(null);
        }

        public MeetingDurationNotAMultipleOfGranularityIssue(String meetingId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.meetingId = meetingId;
        }

        public String getMeetingId() {
            return meetingId;
        }
    }

    @Schema(allOf = { MeetingScheduleIssue.class })
    public static class MeetingLongerThanOfficeDayIssue extends MeetingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("MEETING_LONGER_THAN_OFFICE_DAY");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "Meeting lasts longer than the office hours of any single day, so it can never start and end on the same day.");

        @Schema(description = "The ID of the meeting that does not fit in a day.")
        private String meetingId;

        public MeetingLongerThanOfficeDayIssue() {
            this(null);
        }

        public MeetingLongerThanOfficeDayIssue(String meetingId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.meetingId = meetingId;
        }

        public String getMeetingId() {
            return meetingId;
        }
    }
}
