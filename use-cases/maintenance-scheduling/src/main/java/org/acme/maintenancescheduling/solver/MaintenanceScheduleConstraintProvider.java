package org.acme.maintenancescheduling.solver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import org.acme.maintenancescheduling.domain.Job;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class MaintenanceScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                crewConflict(constraintFactory),
                readyDate(constraintFactory),
                dueDate(constraintFactory),
                // Soft constraints
                beforeIdealEndDate(constraintFactory),
                afterIdealEndDate(constraintFactory),
                tagConflict(constraintFactory),
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    public Constraint crewConflict(ConstraintFactory constraintFactory) {
        // A crew can do at most one maintenance job at the same time.
        return constraintFactory
                .forEachUniquePair(Job.class,
                        equal(Job::getCrew),
                        overlapping(Job::getStartDate, Job::getEndDate))
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        (job1, job2) -> DAYS.between(
                                job1.getStartDate().isAfter(job2.getStartDate())
                                        ? job1.getStartDate() : job2.getStartDate(),
                                job1.getEndDate().isBefore(job2.getEndDate())
                                        ? job1.getEndDate() : job2.getEndDate()))
                .asConstraint("Crew conflict");
    }

    public Constraint readyDate(ConstraintFactory constraintFactory) {
        // Don't start a maintenance job before its ready to start.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getReadyDate() != null
                        && job.getStartDate().isBefore(job.getReadyDate()))
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        job -> DAYS.between(job.getStartDate(), job.getReadyDate()))
                .asConstraint("Ready date");
    }

    public Constraint dueDate(ConstraintFactory constraintFactory) {
        // Don't end a maintenance job after its due.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getDueDate() != null
                        && job.getEndDate().isAfter(job.getDueDate()))
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        job -> DAYS.between(job.getDueDate(), job.getEndDate()))
                .asConstraint("Due date");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint beforeIdealEndDate(ConstraintFactory constraintFactory) {
        // Early maintenance is expensive because the sooner maintenance is done, the sooner it needs to happen again.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getIdealEndDate() != null
                        && job.getEndDate().isBefore(job.getIdealEndDate()))
                .penalizeLong(HardSoftLongScore.ofSoft(1),
                        job -> DAYS.between(job.getEndDate(), job.getIdealEndDate()))
                .asConstraint("Before ideal end date");
    }

    public Constraint afterIdealEndDate(ConstraintFactory constraintFactory) {
        // Late maintenance is risky because delays can push it over the due date.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getIdealEndDate() != null
                        && job.getEndDate().isAfter(job.getIdealEndDate()))
                .penalizeLong(HardSoftLongScore.ofSoft(1_000_000),
                        job -> DAYS.between(job.getIdealEndDate(), job.getEndDate()))
                .asConstraint("After ideal end date");
    }
    
    public Constraint tagConflict(ConstraintFactory constraintFactory) {
        // Avoid overlapping maintenance jobs with the same tag (for example road maintenance in the same area).
        return constraintFactory
                .forEachUniquePair(Job.class,
                        overlapping(Job::getStartDate, Job::getEndDate),
                        // TODO Use intersecting() when available https://github.com/TimefoldAI/timefold-solver/issues/8
                        filtering((job1, job2) -> !Collections.disjoint(
                                job1.getTags(), job2.getTags())))
                .penalizeLong(HardSoftLongScore.ofSoft(1_000),
                        (job1, job2) -> {
                            Set<String> intersection = new HashSet<>(job1.getTags());
                            intersection.retainAll(job2.getTags());
                            long overlap = DAYS.between(
                                    job1.getStartDate().isAfter(job2.getStartDate())
                                            ? job1.getStartDate()  : job2.getStartDate(),
                                    job1.getEndDate().isBefore(job2.getEndDate())
                                            ? job1.getEndDate() : job2.getEndDate());
                            return intersection.size() * overlap;
                        })
                .asConstraint("Tag conflict");
    }

}
