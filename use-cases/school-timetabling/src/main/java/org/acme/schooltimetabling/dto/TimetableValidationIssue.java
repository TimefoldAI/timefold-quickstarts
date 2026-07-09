package org.acme.schooltimetabling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a school timetabling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum TimetableValidationIssue {
    LESSON_ID_MISSING(IssueCode.of("LESSON_ID_MISSING"), IssueSeverity.ERROR,
            "Lesson ID must not be null or blank."),
    DUPLICATE_LESSON_ID(IssueCode.of("DUPLICATE_LESSON_ID"), IssueSeverity.ERROR,
            "Duplicate lesson ID found."),
    TIMESLOT_ID_MISSING(IssueCode.of("TIMESLOT_ID_MISSING"), IssueSeverity.ERROR,
            "Timeslot ID must not be null or blank."),
    DUPLICATE_TIMESLOT_ID(IssueCode.of("DUPLICATE_TIMESLOT_ID"), IssueSeverity.ERROR,
            "Duplicate timeslot ID found."),
    ROOM_ID_MISSING(IssueCode.of("ROOM_ID_MISSING"), IssueSeverity.ERROR,
            "Room ID must not be null or blank."),
    DUPLICATE_ROOM_ID(IssueCode.of("DUPLICATE_ROOM_ID"), IssueSeverity.ERROR,
            "Duplicate room ID found."),
    NON_EXISTING_TIMESLOT_REFERENCE(IssueCode.of("NON_EXISTING_TIMESLOT_REFERENCE"), IssueSeverity.ERROR,
            "Lesson references non-existing timeslot."),
    NON_EXISTING_ROOM_REFERENCE(IssueCode.of("NON_EXISTING_ROOM_REFERENCE"), IssueSeverity.ERROR,
            "Lesson references non-existing room.");

    private final transient IssueType issueType;

    TimetableValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
