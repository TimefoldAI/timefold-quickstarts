package org.acme.tournamentschedule.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.tournamentschedule.dto.TeamAssignmentIdDetail;
import org.acme.tournamentschedule.dto.TeamIdDetail;
import org.acme.tournamentschedule.dto.TournamentScheduleValidationIssue;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class TournamentScheduleIssues {

    private TournamentScheduleIssues() {
    }

    public abstract static class TournamentScheduleIssue extends AbstractIssue {
        protected TournamentScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class TeamIdMissingIssue extends TournamentScheduleIssue {
        private static final IssueType TYPE = TournamentScheduleValidationIssue.TEAM_ID_MISSING.asIssueType();

        public TeamIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTeamIdIssue extends TournamentScheduleIssue {
        private static final IssueType TYPE = TournamentScheduleValidationIssue.DUPLICATE_TEAM_ID.asIssueType();

        public DuplicateTeamIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTeamIdIssue(TeamIdDetail teamIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(teamIdDetail)).toList());
        }
    }

    public static final class TeamAssignmentIdMissingIssue extends TournamentScheduleIssue {
        private static final IssueType TYPE = TournamentScheduleValidationIssue.TEAM_ASSIGNMENT_ID_MISSING.asIssueType();

        public TeamAssignmentIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTeamAssignmentIdIssue extends TournamentScheduleIssue {
        private static final IssueType TYPE =
                TournamentScheduleValidationIssue.DUPLICATE_TEAM_ASSIGNMENT_ID.asIssueType();

        public DuplicateTeamAssignmentIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTeamAssignmentIdIssue(TeamAssignmentIdDetail teamAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(teamAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingTeamReferenceIssue extends TournamentScheduleIssue {
        private static final IssueType TYPE = TournamentScheduleValidationIssue.NON_EXISTING_TEAM_REFERENCE.asIssueType();

        public NonExistingTeamReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTeamReferenceIssue(TeamAssignmentIdDetail teamAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(teamAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingDayReferenceIssue extends TournamentScheduleIssue {
        private static final IssueType TYPE = TournamentScheduleValidationIssue.NON_EXISTING_DAY_REFERENCE.asIssueType();

        public NonExistingDayReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingDayReferenceIssue(TeamAssignmentIdDetail teamAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(teamAssignmentIdDetail)).toList());
        }
    }
}
