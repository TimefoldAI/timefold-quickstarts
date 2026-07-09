package org.acme.sportsleagueschedule.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.sportsleagueschedule.dto.LeagueScheduleValidationIssue;
import org.acme.sportsleagueschedule.dto.MatchIdDetail;
import org.acme.sportsleagueschedule.dto.TeamIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class LeagueScheduleIssues {

    private LeagueScheduleIssues() {
    }

    public abstract static class LeagueScheduleIssue extends AbstractIssue {
        protected LeagueScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class MatchIdMissingIssue extends LeagueScheduleIssue {
        private static final IssueType TYPE = LeagueScheduleValidationIssue.MATCH_ID_MISSING.asIssueType();

        public MatchIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateMatchIdIssue extends LeagueScheduleIssue {
        private static final IssueType TYPE = LeagueScheduleValidationIssue.DUPLICATE_MATCH_ID.asIssueType();

        public DuplicateMatchIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateMatchIdIssue(MatchIdDetail matchIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(matchIdDetail)).toList());
        }
    }

    public static final class TeamIdMissingIssue extends LeagueScheduleIssue {
        private static final IssueType TYPE = LeagueScheduleValidationIssue.TEAM_ID_MISSING.asIssueType();

        public TeamIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTeamIdIssue extends LeagueScheduleIssue {
        private static final IssueType TYPE = LeagueScheduleValidationIssue.DUPLICATE_TEAM_ID.asIssueType();

        public DuplicateTeamIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTeamIdIssue(TeamIdDetail teamIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(teamIdDetail)).toList());
        }
    }

    public static final class NonExistingTeamReferenceIssue extends LeagueScheduleIssue {
        private static final IssueType TYPE = LeagueScheduleValidationIssue.NON_EXISTING_TEAM_REFERENCE.asIssueType();

        public NonExistingTeamReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTeamReferenceIssue(MatchIdDetail matchIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(matchIdDetail)).toList());
        }
    }
}
