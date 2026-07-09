package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a conference scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum ConferenceScheduleValidationIssue {
    TALK_ID_MISSING(IssueCode.of("TALK_ID_MISSING"), IssueSeverity.ERROR,
            "Talk code must not be null or blank."),
    DUPLICATE_TALK_ID(IssueCode.of("DUPLICATE_TALK_ID"), IssueSeverity.ERROR,
            "Duplicate talk code found."),
    SPEAKER_ID_MISSING(IssueCode.of("SPEAKER_ID_MISSING"), IssueSeverity.ERROR,
            "Speaker ID must not be null or blank."),
    DUPLICATE_SPEAKER_ID(IssueCode.of("DUPLICATE_SPEAKER_ID"), IssueSeverity.ERROR,
            "Duplicate speaker ID found."),
    ROOM_ID_MISSING(IssueCode.of("ROOM_ID_MISSING"), IssueSeverity.ERROR,
            "Room ID must not be null or blank."),
    DUPLICATE_ROOM_ID(IssueCode.of("DUPLICATE_ROOM_ID"), IssueSeverity.ERROR,
            "Duplicate room ID found."),
    TIMESLOT_ID_MISSING(IssueCode.of("TIMESLOT_ID_MISSING"), IssueSeverity.ERROR,
            "Timeslot ID must not be null or blank."),
    DUPLICATE_TIMESLOT_ID(IssueCode.of("DUPLICATE_TIMESLOT_ID"), IssueSeverity.ERROR,
            "Duplicate timeslot ID found."),
    NON_EXISTING_TIMESLOT_REFERENCE(IssueCode.of("NON_EXISTING_TIMESLOT_REFERENCE"), IssueSeverity.ERROR,
            "Talk references non-existing timeslot."),
    NON_EXISTING_ROOM_REFERENCE(IssueCode.of("NON_EXISTING_ROOM_REFERENCE"), IssueSeverity.ERROR,
            "Talk references non-existing room."),
    NON_EXISTING_SPEAKER_REFERENCE(IssueCode.of("NON_EXISTING_SPEAKER_REFERENCE"), IssueSeverity.ERROR,
            "Talk references non-existing speaker."),
    NON_EXISTING_TALK_TYPE_REFERENCE(IssueCode.of("NON_EXISTING_TALK_TYPE_REFERENCE"), IssueSeverity.ERROR,
            "Talk references non-existing talk type.");

    private final transient IssueType issueType;

    ConferenceScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
