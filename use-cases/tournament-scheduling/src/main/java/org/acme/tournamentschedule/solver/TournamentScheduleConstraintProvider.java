package org.acme.tournamentschedule.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.loadBalance;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;

public class TournamentScheduleConstraintProvider implements ConstraintProvider {

    public static final String ONE_ASSIGNMENT_PER_DATE_PER_TEAM = "One assignment per date per team";
    public static final String UNAVAILABILITY_PENALTY = "Unavailability penalty";
    public static final String FAIR_ASSIGNMENT_COUNT_PER_TEAM = "Fair assignment count per team";
    public static final String EVENLY_CONFRONTATION_COUNT = "Evenly confrontation count";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                oneAssignmentPerDatePerTeam(constraintFactory),
                unavailabilityPenalty(constraintFactory),
                fairAssignmentCountPerTeam(constraintFactory),
                evenlyConfrontationCount(constraintFactory)
        };
    }

    Constraint oneAssignmentPerDatePerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getTeam),
                        equal(TeamAssignment::getDay),
                        lessThan(TeamAssignment::getId))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(ONE_ASSIGNMENT_PER_DATE_PER_TEAM, ONE_ASSIGNMENT_PER_DATE_PER_TEAM,
                        "A team can be assigned at most once per day.",
                        TournamentScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    Constraint unavailabilityPenalty(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(UnavailabilityPenalty.class)
                .ifExists(TeamAssignment.class,
                        equal(UnavailabilityPenalty::getTeam, TeamAssignment::getTeam),
                        equal(UnavailabilityPenalty::getDay, TeamAssignment::getDay))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(UNAVAILABILITY_PENALTY, UNAVAILABILITY_PENALTY,
                        "A team must not be assigned on a day on which it is unavailable.",
                        TournamentScheduleConstraintGroup.AVAILABILITY));
    }

    Constraint fairAssignmentCountPerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .groupBy(loadBalance(TeamAssignment::getTeam))
                .penalize(HardMediumSoftScore.ONE_MEDIUM, TournamentScheduleConstraintProvider::scaledUnfairness)
                .asConstraint(new ConstraintInfo(FAIR_ASSIGNMENT_COUNT_PER_TEAM, FAIR_ASSIGNMENT_COUNT_PER_TEAM,
                        "Balance the number of assignments across all teams.",
                        TournamentScheduleConstraintGroup.FAIRNESS));
    }

    Constraint evenlyConfrontationCount(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getDay),
                        lessThan(assignment -> assignment.getTeam().getId()))
                .groupBy(loadBalance(
                        (assignment, otherAssignment) -> new TeamPair(assignment.getTeam(), otherAssignment.getTeam())))
                .penalize(HardMediumSoftScore.ONE_SOFT, TournamentScheduleConstraintProvider::scaledUnfairness)
                .asConstraint(new ConstraintInfo(EVENLY_CONFRONTATION_COUNT, EVENLY_CONFRONTATION_COUNT,
                        "Balance how often each pair of teams confronts each other.",
                        TournamentScheduleConstraintGroup.FAIRNESS));
    }

    /**
     * {@link LoadBalance#unfairness()} returns a {@link java.math.BigDecimal}; scale it to a long so it can be used with
     * the {@link HardMediumSoftScore} score type, which the SDK service layer supports for feasibility termination.
     */
    private static long scaledUnfairness(LoadBalance<?> loadBalance) {
        return loadBalance.unfairness().movePointRight(6).longValue();
    }

    static final class TeamPair {
        private final Team first;
        private final Team second;

        TeamPair(Team first, Team second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TeamPair other)) {
                return false;
            }
            return java.util.Objects.equals(first, other.first) && java.util.Objects.equals(second, other.second);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(first, second);
        }
    }
}
