package org.acme.foodpackaging.solver;

import java.time.Duration;
import java.time.LocalDateTime;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.foodpackaging.domain.Job;

public class FoodPackagingConstraintProvider implements ConstraintProvider {

    public static final String MAX_END_DATE_TIME = "Max end date time";
    public static final String OPERATOR_CLEANING_CONFLICT = "Operator cleaning conflict";
    public static final String IDEAL_END_DATE_TIME = "Ideal end date time";
    public static final String MAXIMIZE_JOBS_ASSIGNED = "Maximize jobs assigned";
    public static final String MINIMIZE_MAKESPAN = "Minimize makespan";

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

    Constraint maxEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getMaxEndTime().isBefore(job.getEndDateTime()))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        job -> (int) Duration.between(job.getMaxEndTime(), job.getEndDateTime()).toMinutes())
                .asConstraint(new ConstraintInfo(MAX_END_DATE_TIME, MAX_END_DATE_TIME,
                        "A job must finish before its maximum end time.",
                        FoodPackagingConstraintGroup.SCHEDULE_FEASIBILITY));
    }

    Constraint operatorCleaningConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                Job.class,
                Joiners.equal(Job::getLineOperator),
                Joiners.overlapping(Job::getStartCleaningDateTime, Job::getStartProductionDateTime))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (j1, j2) -> (int) overlapMinutes(
                                j1.getStartCleaningDateTime(), j1.getStartProductionDateTime(),
                                j2.getStartCleaningDateTime(), j2.getStartProductionDateTime()))
                .asConstraint(new ConstraintInfo(OPERATOR_CLEANING_CONFLICT, OPERATOR_CLEANING_CONFLICT,
                        "An operator cannot be assigned to a job during their cleaning time.",
                        FoodPackagingConstraintGroup.SCHEDULE_FEASIBILITY));
    }

    private static long overlapMinutes(LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime start = start1.isAfter(start2) ? start1 : start2;
        LocalDateTime end = end1.isBefore(end2) ? end1 : end2;
        return Duration.between(start, end).toMinutes();
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    Constraint idealEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getIdealEndTime().isBefore(job.getEndDateTime()))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        job -> (int) Duration.between(job.getIdealEndTime(), job.getEndDateTime()).toMinutes())
                .asConstraint(new ConstraintInfo(IDEAL_END_DATE_TIME, IDEAL_END_DATE_TIME,
                        "A job should ideally finish before its ideal end time.",
                        FoodPackagingConstraintGroup.DELIVERY_PERFORMANCE));
    }

    Constraint maximizeJobsAssigned(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Job.class)
                .filter(job -> job.getLine() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, job -> (int) job.getDuration().toMinutes())
                .asConstraint(new ConstraintInfo(MAXIMIZE_JOBS_ASSIGNED, MAXIMIZE_JOBS_ASSIGNED,
                        "Assign as many jobs as possible.",
                        FoodPackagingConstraintGroup.DELIVERY_PERFORMANCE));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint minimizeMakespan(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getNextJob() == null)
                .penalize(HardMediumSoftScore.ONE_SOFT, job -> {
                    long minutes =
                            Duration.between(job.getLine().getStartDateTime(), job.getEndDateTime()).toMinutes();
                    return (int) (minutes * minutes);
                })
                .asConstraint(new ConstraintInfo(MINIMIZE_MAKESPAN, MINIMIZE_MAKESPAN,
                        "Minimize the total production time.",
                        FoodPackagingConstraintGroup.EFFICIENCY));
    }
}
