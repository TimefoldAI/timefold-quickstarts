package org.acme.foodpackaging.solver;

import java.time.Duration;
import java.time.OffsetDateTime;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.PackagingScheduleConstraintProperties;
import org.acme.foodpackaging.domain.justification.PackagingScheduleJustification.JobEndsAfterIdealEndTimeJustification;
import org.acme.foodpackaging.domain.justification.PackagingScheduleJustification.JobEndsAfterMaxEndTimeJustification;
import org.acme.foodpackaging.domain.justification.PackagingScheduleJustification.LineMakespanJustification;
import org.acme.foodpackaging.domain.justification.PackagingScheduleJustification.OperatorCleaningOverlapJustification;
import org.acme.foodpackaging.domain.justification.PackagingScheduleJustification.UnassignedJobJustification;

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
                .justifyWith((job, score) -> JobEndsAfterMaxEndTimeJustification.of(job))
                .asConstraint(new ConstraintInfo(PackagingScheduleConstraintProperties.MAX_END_DATE_TIME,
                        PackagingScheduleConstraintProperties.MAX_END_DATE_TIME,
                        "A job must finish before its maximum end time.",
                        PackagingScheduleConstraintGroup.ON_TIME_DELIVERY));
    }

    protected Constraint operatorCleaningConflict(ConstraintFactory factory) {
        // Joins on the lineOperator shadow variable rather than on getLine().getOperator(), so an
        // unassigned job (which has no line) cannot trip over a null line here.
        return factory.forEachUniquePair(
                Job.class,
                Joiners.equal(Job::getLineOperator),
                Joiners.overlapping(Job::getStartCleaningDateTime, Job::getStartProductionDateTime))
                // Two jobs on lines that have no operator yet are not a conflict, even though both their
                // (absent) operators compare equal.
                .filter((job, otherJob) -> job.getLineOperator() != null)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (job, otherJob) -> overlapMinutes(
                                job.getStartCleaningDateTime(), job.getStartProductionDateTime(),
                                otherJob.getStartCleaningDateTime(), otherJob.getStartProductionDateTime()))
                .justifyWith((job, otherJob, score) -> OperatorCleaningOverlapJustification.of(job, otherJob,
                        overlapMinutes(job.getStartCleaningDateTime(), job.getStartProductionDateTime(),
                                otherJob.getStartCleaningDateTime(), otherJob.getStartProductionDateTime())))
                .asConstraint(new ConstraintInfo(PackagingScheduleConstraintProperties.OPERATOR_CLEANING_CONFLICT,
                        PackagingScheduleConstraintProperties.OPERATOR_CLEANING_CONFLICT,
                        "An operator must not have to clean two of their lines at the same time.",
                        PackagingScheduleConstraintGroup.OPERATOR_AVAILABILITY));
    }

    private static long overlapMinutes(OffsetDateTime start1, OffsetDateTime end1,
            OffsetDateTime start2, OffsetDateTime end2) {
        var start = start1.isAfter(start2) ? start1 : start2;
        var end = end1.isBefore(end2) ? end1 : end2;
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
                .justifyWith((job, score) -> JobEndsAfterIdealEndTimeJustification.of(job))
                .asConstraint(new ConstraintInfo(PackagingScheduleConstraintProperties.IDEAL_END_DATE_TIME,
                        PackagingScheduleConstraintProperties.IDEAL_END_DATE_TIME,
                        "A job should finish before its ideal end time.",
                        PackagingScheduleConstraintGroup.ON_TIME_DELIVERY));
    }

    protected Constraint maximizeJobsAssigned(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Job.class)
                .filter(job -> job.getLine() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, job -> job.getDuration().toMinutes())
                .justifyWith((job, score) -> UnassignedJobJustification.of(job))
                .asConstraint(new ConstraintInfo(PackagingScheduleConstraintProperties.MAXIMIZE_JOBS_ASSIGNED,
                        PackagingScheduleConstraintProperties.MAXIMIZE_JOBS_ASSIGNED,
                        "Every job should be produced on one of the lines.",
                        PackagingScheduleConstraintGroup.JOB_ASSIGNMENT));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeMakespan(ConstraintFactory factory) {
        // Only the last job of a line is penalized, quadratically on that line's makespan, so evening the
        // lines out beats piling everything onto one line even when the total production time is the same.
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getNextJob() == null)
                .penalize(HardMediumSoftScore.ONE_SOFT, job -> {
                    long minutes = Duration.between(job.getLine().getStartDateTime(), job.getEndDateTime()).toMinutes();
                    return minutes * minutes;
                })
                .justifyWith((job, score) -> LineMakespanJustification.of(job))
                .asConstraint(new ConstraintInfo(PackagingScheduleConstraintProperties.MINIMIZE_MAKESPAN,
                        PackagingScheduleConstraintProperties.MINIMIZE_MAKESPAN,
                        "Every line should finish producing as early as possible.",
                        PackagingScheduleConstraintGroup.LINE_THROUGHPUT));
    }
}
