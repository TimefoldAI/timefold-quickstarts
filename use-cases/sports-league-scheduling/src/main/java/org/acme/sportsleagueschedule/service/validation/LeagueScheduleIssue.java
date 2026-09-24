package org.acme.sportsleagueschedule.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a sports league scheduling input.",
        oneOf = {
                LeagueScheduleIssue.DuplicateTeamIdIssue.class,
                LeagueScheduleIssue.DuplicateMatchIdIssue.class,
                LeagueScheduleIssue.DuplicateRoundIndexIssue.class,
                LeagueScheduleIssue.NonExistingTeamReferenceIssue.class,
                LeagueScheduleIssue.SameHomeAndAwayTeamIssue.class,
                LeagueScheduleIssue.NonExistingRoundReferenceIssue.class,
                LeagueScheduleIssue.MissingDistanceIssue.class
        })
public abstract class LeagueScheduleIssue extends AbstractIssue {

    protected LeagueScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class DuplicateTeamIdIssue extends LeagueScheduleIssue {

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

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class DuplicateMatchIdIssue extends LeagueScheduleIssue {

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

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class DuplicateRoundIndexIssue extends LeagueScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_ROUND_INDEX");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate round index found.");

        @Schema(description = "The index of the duplicated round.")
        private Integer roundIndex;

        public DuplicateRoundIndexIssue() {
            this(null);
        }

        public DuplicateRoundIndexIssue(Integer roundIndex) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.roundIndex = roundIndex;
        }

        public Integer getRoundIndex() {
            return roundIndex;
        }
    }

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class NonExistingTeamReferenceIssue extends LeagueScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TEAM_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Match refers to a team ID that does not exist.");

        @Schema(description = "The ID of the match with the unknown team reference.")
        private String matchId;

        public NonExistingTeamReferenceIssue() {
            this(null);
        }

        public NonExistingTeamReferenceIssue(String matchId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.matchId = matchId;
        }

        public String getMatchId() {
            return matchId;
        }
    }

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class SameHomeAndAwayTeamIssue extends LeagueScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("SAME_HOME_AND_AWAY_TEAM");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("A match cannot have the same team playing at home and away.");

        @Schema(description = "The ID of the match playing a team against itself.")
        private String matchId;

        public SameHomeAndAwayTeamIssue() {
            this(null);
        }

        public SameHomeAndAwayTeamIssue(String matchId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.matchId = matchId;
        }

        public String getMatchId() {
            return matchId;
        }
    }

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class NonExistingRoundReferenceIssue extends LeagueScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_ROUND_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Match is assigned to a round index that does not exist.");

        @Schema(description = "The ID of the match with the unknown round reference.")
        private String matchId;

        public NonExistingRoundReferenceIssue() {
            this(null);
        }

        public NonExistingRoundReferenceIssue(String matchId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.matchId = matchId;
        }

        public String getMatchId() {
            return matchId;
        }
    }

    @Schema(allOf = { LeagueScheduleIssue.class })
    public static class MissingDistanceIssue extends LeagueScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("MISSING_DISTANCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Team has no travel distance to at least one other team.");

        @Schema(description = "The ID of the team with an incomplete distance map.")
        private String teamId;

        public MissingDistanceIssue() {
            this(null);
        }

        public MissingDistanceIssue(String teamId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.teamId = teamId;
        }

        public String getTeamId() {
            return teamId;
        }
    }
}
