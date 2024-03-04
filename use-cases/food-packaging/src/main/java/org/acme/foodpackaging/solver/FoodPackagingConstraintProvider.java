package org.acme.foodpackaging.solver;

import java.time.Duration;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
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
                dueDateTime(factory),
                // Medium constraints
                idealEndDateTime(factory),
                // Soft constraints
                operatorCleaningConflict(factory),
                minimizeAndLoadBalanceMakeSpan(factory)
//                minimizeCleaningDuration(factory),
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint dueDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getDueDateTime().isBefore(job.getEndDateTime()))
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD,
                        job -> Duration.between(job.getDueDateTime(), job.getEndDateTime()).toMinutes())
                .asConstraint("Due date time");
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    protected Constraint idealEndDateTime(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getIdealEndDateTime().isBefore(job.getEndDateTime()))
                .penalizeLong(HardMediumSoftLongScore.ONE_MEDIUM,
                        job -> Duration.between(job.getIdealEndDateTime(), job.getEndDateTime()).toMinutes())
                .asConstraint("Ideal end date time");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    // TODO Currently dwarfed by minimizeAndLoadBalanceMakeSpan in the same score level, because that squares
    protected Constraint operatorCleaningConflict(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job ->job.getLine() != null)
                .join(factory.forEach(Job.class).filter(job ->job.getLine() != null),
                        Joiners.equal(job -> job.getLine().getOperator()),
                        Joiners.overlapping(Job::getStartCleaningDateTime, Job::getStartProductionDateTime),
                        Joiners.lessThan(Job::getId))
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT, (job1, job2) -> Duration.between(
                        (job1.getStartCleaningDateTime().compareTo(job2.getStartCleaningDateTime()) > 0)
                               ? job1.getStartCleaningDateTime() : job2.getStartCleaningDateTime(),
                        (job1.getStartProductionDateTime().compareTo(job2.getStartProductionDateTime()) < 0)
                                ? job1.getStartProductionDateTime() : job2.getStartProductionDateTime()
                ).toMinutes())
                .asConstraint("Operator cleaning conflict");
    }

    protected Constraint minimizeAndLoadBalanceMakeSpan(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getLine() != null && job.getNextJob() == null)
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT, job -> {
                    long minutes = Duration.between(job.getLine().getStartDateTime(), job.getEndDateTime()).toMinutes();
                    return minutes * minutes;
                })
                .asConstraint("Minimize and load balance make span");
    }

    // TODO Currently dwarfed by minimizeAndLoadBalanceMakeSpan in the same score level, because that squares
    protected Constraint minimizeCleaningDuration(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .filter(job -> job.getStartProductionDateTime() != null)
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT, job -> job.getPriority()
                        * Duration.between(job.getStartCleaningDateTime(), job.getStartProductionDateTime()).toMinutes())
                .asConstraint("Minimize cleaning duration");
    }

}
