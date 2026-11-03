package org.acme.tournamentschedule.service.validation;

import java.time.LocalDate;
import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a tournament scheduling input.",
        oneOf = {
                TournamentScheduleIssue.DuplicateTeamIdIssue.class,
                TournamentScheduleIssue.DuplicateMatchIdIssue.class,
                TournamentScheduleIssue.NonExistingTeamReferenceInMatchIssue.class,
                TournamentScheduleIssue.NonExistingTeamReferenceInUnavailabilityIssue.class,
                TournamentScheduleIssue.DuplicateUnavailabilityIssue.class
        })
public abstract class TournamentScheduleIssue extends AbstractIssue {

    protected TournamentScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { TournamentScheduleIssue.class })
    public static class DuplicateTeamIdIssue extends TournamentScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TEAM_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate team ID found.");

        @Schema(description = "The ID of the duplicated team.")
        private String teamId;

        public DuplicateTeamIdIssue() {
            this(null);
        }

        public DuplicateTeamIdIssue(String teamId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.teamId = teamId;
        }

        public String getTeamId() {
            return teamId;
        }
    }

    @Schema(allOf = { TournamentScheduleIssue.class })
    public static class DuplicateMatchIdIssue extends TournamentScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_MATCH_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate match ID found.");

        @Schema(description = "The ID of the duplicated match.")
        private String matchId;

        public DuplicateMatchIdIssue() {
            this(null);
        }

        public DuplicateMatchIdIssue(String matchId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.matchId = matchId;
        }

        public String getMatchId() {
            return matchId;
        }
    }

    @Schema(allOf = { TournamentScheduleIssue.class })
    public static class NonExistingTeamReferenceInMatchIssue extends TournamentScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TEAM_REFERENCE_IN_MATCH");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Match refers to a team ID that does not exist.");

        @Schema(description = "The ID of the match with the unknown team reference.")
        private String matchId;

        public NonExistingTeamReferenceInMatchIssue() {
            this(null);
        }

        public NonExistingTeamReferenceInMatchIssue(String matchId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.matchId = matchId;
        }

        public String getMatchId() {
            return matchId;
        }
    }

    @Schema(allOf = { TournamentScheduleIssue.class })
    public static class NonExistingTeamReferenceInUnavailabilityIssue extends TournamentScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TEAM_REFERENCE_IN_UNAVAILABILITY");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Unavailability refers to a team ID that does not exist.");

        @Schema(description = "The unknown team ID referenced by the unavailability entry.")
        private String teamId;

        public NonExistingTeamReferenceInUnavailabilityIssue() {
            this(null);
        }

        public NonExistingTeamReferenceInUnavailabilityIssue(String teamId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.teamId = teamId;
        }

        public String getTeamId() {
            return teamId;
        }
    }

    @Schema(allOf = { TournamentScheduleIssue.class })
    public static class DuplicateUnavailabilityIssue extends TournamentScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_UNAVAILABILITY");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("The same team is reported unavailable on the same day more than once.");

        @Schema(description = "The ID of the team with the duplicated unavailability.")
        private String teamId;
        @Schema(description = "The duplicated unavailability day, in ISO-8601 date format.")
        private LocalDate date;

        public DuplicateUnavailabilityIssue() {
            this(null, null);
        }

        public DuplicateUnavailabilityIssue(String teamId, LocalDate date) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.teamId = teamId;
            this.date = date;
        }

        public String getTeamId() {
            return teamId;
        }

        public LocalDate getDate() {
            return date;
        }
    }
}
