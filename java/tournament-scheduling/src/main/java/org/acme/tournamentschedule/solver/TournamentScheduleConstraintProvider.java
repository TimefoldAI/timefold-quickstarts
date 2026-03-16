package org.acme.tournamentschedule.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.loadBalance;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

import ai.timefold.solver.core.api.score.HardMediumSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;

import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;

public class TournamentScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                oneAssignmentPerDatePerTeam(constraintFactory),
                unavailabilityPenalty(constraintFactory),

                // Medium constraints
                fairAssignmentCountPerTeam(constraintFactory),

                // Soft constraints
                evenlyConfrontationCount(constraintFactory)
        };
    }

    Constraint oneAssignmentPerDatePerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getTeam),
                        equal(TeamAssignment::getDay),
                        lessThan(TeamAssignment::getId))
                .penalize(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("oneAssignmentPerDatePerTeam");
    }

    Constraint unavailabilityPenalty(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(UnavailabilityPenalty.class)
                .ifExists(TeamAssignment.class,
                        equal(UnavailabilityPenalty::getTeam, TeamAssignment::getTeam),
                        equal(UnavailabilityPenalty::getDay, TeamAssignment::getDay))
                .penalize(HardMediumSoftBigDecimalScore.ONE_HARD)
                .asConstraint("unavailabilityPenalty");
    }

    Constraint fairAssignmentCountPerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .groupBy(loadBalance(TeamAssignment::getTeam))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_MEDIUM, LoadBalance::unfairness)
                .asConstraint("fairAssignmentCountPerTeam");
    }

    Constraint evenlyConfrontationCount(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getDay),
                        lessThan(assignment -> assignment.getTeam().getId()))
                .groupBy(loadBalance(
                        (assignment, otherAssignment) -> new Pair<>(assignment.getTeam(), otherAssignment.getTeam())))
                .penalizeBigDecimal(HardMediumSoftBigDecimalScore.ONE_SOFT, LoadBalance::unfairness)
                .asConstraint("evenlyConfrontationCount");
    }

    public record Pair<A, B>(A key, B value) {

    }

}
