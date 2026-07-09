package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a meeting scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum MeetingScheduleValidationIssue {
    MEETING_ID_MISSING(IssueCode.of("MEETING_ID_MISSING"), IssueSeverity.ERROR,
            "Meeting ID must not be null or blank."),
    DUPLICATE_MEETING_ID(IssueCode.of("DUPLICATE_MEETING_ID"), IssueSeverity.ERROR,
            "Duplicate meeting ID found."),
    ROOM_ID_MISSING(IssueCode.of("ROOM_ID_MISSING"), IssueSeverity.ERROR,
            "Room ID must not be null or blank."),
    DUPLICATE_ROOM_ID(IssueCode.of("DUPLICATE_ROOM_ID"), IssueSeverity.ERROR,
            "Duplicate room ID found."),
    TIME_GRAIN_ID_MISSING(IssueCode.of("TIME_GRAIN_ID_MISSING"), IssueSeverity.ERROR,
            "Time grain ID must not be null or blank."),
    DUPLICATE_TIME_GRAIN_ID(IssueCode.of("DUPLICATE_TIME_GRAIN_ID"), IssueSeverity.ERROR,
            "Duplicate time grain ID found."),
    PERSON_ID_MISSING(IssueCode.of("PERSON_ID_MISSING"), IssueSeverity.ERROR,
            "Person ID must not be null or blank."),
    DUPLICATE_PERSON_ID(IssueCode.of("DUPLICATE_PERSON_ID"), IssueSeverity.ERROR,
            "Duplicate person ID found."),
    MEETING_ASSIGNMENT_ID_MISSING(IssueCode.of("MEETING_ASSIGNMENT_ID_MISSING"), IssueSeverity.ERROR,
            "Meeting assignment ID must not be null or blank."),
    DUPLICATE_MEETING_ASSIGNMENT_ID(IssueCode.of("DUPLICATE_MEETING_ASSIGNMENT_ID"), IssueSeverity.ERROR,
            "Duplicate meeting assignment ID found."),
    NON_EXISTING_MEETING_REFERENCE(IssueCode.of("NON_EXISTING_MEETING_REFERENCE"), IssueSeverity.ERROR,
            "Meeting assignment references non-existing meeting."),
    NON_EXISTING_TIME_GRAIN_REFERENCE(IssueCode.of("NON_EXISTING_TIME_GRAIN_REFERENCE"), IssueSeverity.ERROR,
            "Meeting assignment references non-existing time grain."),
    NON_EXISTING_ROOM_REFERENCE(IssueCode.of("NON_EXISTING_ROOM_REFERENCE"), IssueSeverity.ERROR,
            "Meeting assignment references non-existing room."),
    NON_EXISTING_ATTENDANCE_PERSON_REFERENCE(IssueCode.of("NON_EXISTING_ATTENDANCE_PERSON_REFERENCE"), IssueSeverity.ERROR,
            "Meeting references non-existing attendance person.");

    private final transient IssueType issueType;

    MeetingScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
