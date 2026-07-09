package org.acme.tournamentschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a tournament scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum TournamentScheduleValidationIssue {
    TEAM_ID_MISSING(IssueCode.of("TEAM_ID_MISSING"), IssueSeverity.ERROR,
            "Team ID must not be null or blank."),
    DUPLICATE_TEAM_ID(IssueCode.of("DUPLICATE_TEAM_ID"), IssueSeverity.ERROR,
            "Duplicate team ID found."),
    TEAM_ASSIGNMENT_ID_MISSING(IssueCode.of("TEAM_ASSIGNMENT_ID_MISSING"), IssueSeverity.ERROR,
            "Team assignment ID must not be null or blank."),
    DUPLICATE_TEAM_ASSIGNMENT_ID(IssueCode.of("DUPLICATE_TEAM_ASSIGNMENT_ID"), IssueSeverity.ERROR,
            "Duplicate team assignment ID found."),
    NON_EXISTING_TEAM_REFERENCE(IssueCode.of("NON_EXISTING_TEAM_REFERENCE"), IssueSeverity.ERROR,
            "Team assignment references non-existing team."),
    NON_EXISTING_DAY_REFERENCE(IssueCode.of("NON_EXISTING_DAY_REFERENCE"), IssueSeverity.ERROR,
            "Team assignment references non-existing day.");

    private final transient IssueType issueType;

    TournamentScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
