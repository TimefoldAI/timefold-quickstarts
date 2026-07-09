package org.acme.sportsleagueschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a sports league scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum LeagueScheduleValidationIssue {
    MATCH_ID_MISSING(IssueCode.of("MATCH_ID_MISSING"), IssueSeverity.ERROR,
            "Match ID must not be null or blank."),
    DUPLICATE_MATCH_ID(IssueCode.of("DUPLICATE_MATCH_ID"), IssueSeverity.ERROR,
            "Duplicate match ID found."),
    TEAM_ID_MISSING(IssueCode.of("TEAM_ID_MISSING"), IssueSeverity.ERROR,
            "Team ID must not be null or blank."),
    DUPLICATE_TEAM_ID(IssueCode.of("DUPLICATE_TEAM_ID"), IssueSeverity.ERROR,
            "Duplicate team ID found."),
    NON_EXISTING_TEAM_REFERENCE(IssueCode.of("NON_EXISTING_TEAM_REFERENCE"), IssueSeverity.ERROR,
            "Match references non-existing team.");

    private final transient IssueType issueType;

    LeagueScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
