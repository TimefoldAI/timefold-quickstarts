package org.acme.tournamentschedule.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.loadBalance;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.tournamentschedule.domain.Confrontation;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.TournamentScheduleConstraintProperties;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.acme.tournamentschedule.domain.justification.TournamentScheduleJustification.EvenConfrontationCountJustification;
import org.acme.tournamentschedule.domain.justification.TournamentScheduleJustification.FairAssignmentCountJustification;
import org.acme.tournamentschedule.domain.justification.TournamentScheduleJustification.OneAssignmentPerDatePerTeamJustification;
import org.acme.tournamentschedule.domain.justification.TournamentScheduleJustification.UnavailabilityPenaltyJustification;

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
                        equal(TeamAssignment::getMatchDate),
                        lessThan(TeamAssignment::getId))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .justifyWith((assignment, otherAssignment, score) -> OneAssignmentPerDatePerTeamJustification
                        .of(assignment, otherAssignment))
                .asConstraint(new ConstraintInfo(TournamentScheduleConstraintProperties.ONE_ASSIGNMENT_PER_DATE_PER_TEAM,
                        TournamentScheduleConstraintProperties.ONE_ASSIGNMENT_PER_DATE_PER_TEAM,
                        "A team can only have one assignment per date.",
                        TournamentScheduleConstraintGroup.SCHEDULING_CONFLICTS));
    }

    Constraint unavailabilityPenalty(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(UnavailabilityPenalty.class)
                .ifExists(TeamAssignment.class,
                        equal(UnavailabilityPenalty::team, TeamAssignment::getTeam),
                        equal(UnavailabilityPenalty::date, TeamAssignment::getMatchDate))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .justifyWith((penalty, score) -> UnavailabilityPenaltyJustification.of(penalty))
                .asConstraint(new ConstraintInfo(TournamentScheduleConstraintProperties.UNAVAILABILITY_PENALTY,
                        TournamentScheduleConstraintProperties.UNAVAILABILITY_PENALTY,
                        "A team cannot be assigned during their unavailability period.",
                        TournamentScheduleConstraintGroup.SCHEDULING_CONFLICTS));
    }

    Constraint fairAssignmentCountPerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .groupBy(loadBalance(TeamAssignment::getTeam))
                .penalize(HardMediumSoftScore.ONE_MEDIUM, TournamentScheduleConstraintProvider::unfairnessWeight)
                .justifyWith((loadBalance, score) -> FairAssignmentCountJustification.of(loadBalance))
                .asConstraint(new ConstraintInfo(TournamentScheduleConstraintProperties.FAIR_ASSIGNMENT_COUNT_PER_TEAM,
                        TournamentScheduleConstraintProperties.FAIR_ASSIGNMENT_COUNT_PER_TEAM,
                        "Fairly distribute the number of assignments across all teams.",
                        TournamentScheduleConstraintGroup.FAIRNESS));
    }

    Constraint evenlyConfrontationCount(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getMatchDate),
                        lessThan(assignment -> assignment.getTeam().id()))
                .groupBy(loadBalance(
                        (assignment, otherAssignment) -> new Confrontation(assignment.getTeam(), otherAssignment.getTeam())))
                .penalize(HardMediumSoftScore.ONE_SOFT, TournamentScheduleConstraintProvider::unfairnessWeight)
                .justifyWith((loadBalance, score) -> EvenConfrontationCountJustification.of(loadBalance))
                .asConstraint(new ConstraintInfo(TournamentScheduleConstraintProperties.EVEN_CONFRONTATION_COUNT,
                        TournamentScheduleConstraintProperties.EVEN_CONFRONTATION_COUNT,
                        "Balance the number of confrontations between each pair of teams.",
                        TournamentScheduleConstraintGroup.MATCH_BALANCE));
    }

    /**
     * {@link HardMediumSoftScore} only impacts in whole (long) units, but {@link LoadBalance#unfairness()} is a
     * {@link BigDecimal} with a fractional part. Scaling it up before rounding keeps three digits of that fraction
     * significant, so two schedules with a different (if slight) imbalance still compare as different scores.
     */
    private static final BigDecimal UNFAIRNESS_SCALE = BigDecimal.valueOf(1000);

    private static long unfairnessWeight(LoadBalance<?> loadBalance) {
        return loadBalance.unfairness().multiply(UNFAIRNESS_SCALE).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
