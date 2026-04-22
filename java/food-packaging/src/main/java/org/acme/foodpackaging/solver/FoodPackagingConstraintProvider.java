package org.acme.foodpackaging.solver;

import java.time.Duration;
import java.time.LocalDateTime;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import org.acme.foodpackaging.domain.Job;

public class FoodPackagingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard constraints
                maxEndDateTime(factory),
                operatorCleaningConflict(factory),

                // Medium constraints
                idealEndDateTime(factory),
                maximizeJobsAssigned(factory),

                // Soft constraints
                minimizeMakespan(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint maxEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getMaxEndTime().isBefore(job.getEndDateTime()))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        job -> Duration.between(job.getMaxEndTime(), job.getEndDateTime()).toMinutes())
                .asConstraint("Max end date time");
    }

    protected Constraint operatorCleaningConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                        Job.class,
                        Joiners.equal(job -> job.getLine().getOperator()),
                        Joiners.overlapping(Job::getStartCleaningDateTime, Job::getStartProductionDateTime)
                )
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (j1, j2) -> overlapMinutes(
                                j1.getStartCleaningDateTime(), j1.getStartProductionDateTime(),
                                j2.getStartCleaningDateTime(), j2.getStartProductionDateTime()
                        ))
                .asConstraint("Operator cleaning conflict");
    }

    private static long overlapMinutes(LocalDateTime start1, LocalDateTime end1,
                                       LocalDateTime start2, LocalDateTime end2) {
        var start = start1.isAfter(start2) ? start1 : start2;
        var end   = end1.isBefore(end2) ? end1 : end2;
        return Duration.between(start, end).toMinutes();
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    protected Constraint idealEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getIdealEndTime().isBefore(job.getEndDateTime()))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        job -> Duration.between(job.getIdealEndTime(), job.getEndDateTime()).toMinutes())
                .asConstraint("Ideal end date time");
    }

    protected Constraint maximizeJobsAssigned(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Job.class)
                .filter(job -> job.getLine() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, job -> job.getDuration().toMinutes())
                .asConstraint("Maximize jobs assigned");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeMakespan(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getNextJob() == null)
                .penalize(HardMediumSoftScore.ONE_SOFT, job -> {
                    long minutes = Duration.between(job.getLine().getStartDateTime(), job.getEndDateTime()).toMinutes();
                    return minutes * minutes;
                })
                .asConstraint("Minimize make span");
    }
}
