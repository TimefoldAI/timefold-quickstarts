package org.acme.maintenancescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;
import static java.time.temporal.ChronoUnit.DAYS;

import java.util.HashSet;
import java.util.Set;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.maintenancescheduling.domain.Job;

public class MaintenanceScheduleConstraintProvider implements ConstraintProvider {

    public static final String CREW_CONFLICT = "Crew conflict";
    public static final String MIN_START_DATE = "Min start date";
    public static final String MAX_END_DATE = "Max end date";
    public static final String BEFORE_IDEAL_END_DATE = "Before ideal end date";
    public static final String AFTER_IDEAL_END_DATE = "After ideal end date";
    public static final String TAG_CONFLICT = "Tag conflict";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                crewConflict(constraintFactory),
                minStartDate(constraintFactory),
                maxEndDate(constraintFactory),

                // Soft constraints
                beforeIdealEndDate(constraintFactory),
                afterIdealEndDate(constraintFactory),
                tagConflict(constraintFactory),
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint crewConflict(ConstraintFactory constraintFactory) {
        // A crew can do at most one maintenance job at the same time.
        return constraintFactory
                .forEachUniquePair(Job.class,
                        equal(Job::getCrew),
                        overlapping(Job::getStartDate, Job::getEndDate))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (job1, job2) -> (int) DAYS.between(
                                job1.getStartDate().isAfter(job2.getStartDate())
                                        ? job1.getStartDate()
                                        : job2.getStartDate(),
                                job1.getEndDate().isBefore(job2.getEndDate())
                                        ? job1.getEndDate()
                                        : job2.getEndDate()))
                .asConstraint(new ConstraintInfo(CREW_CONFLICT, CREW_CONFLICT,
                        "A crew can do at most one maintenance job at the same time.",
                        MaintenanceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }

    Constraint minStartDate(ConstraintFactory constraintFactory) {
        // Don't start a maintenance job before it is ready to start.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getMinStartDate() != null
                        && job.getStartDate().isBefore(job.getMinStartDate()))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        job -> (int) DAYS.between(job.getStartDate(), job.getMinStartDate()))
                .asConstraint(new ConstraintInfo(MIN_START_DATE, MIN_START_DATE,
                        "Don't start a maintenance job before it is ready to start.",
                        MaintenanceScheduleConstraintGroup.DEADLINES));
    }

    Constraint maxEndDate(ConstraintFactory constraintFactory) {
        // Don't end a maintenance job after it is due.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getMaxEndDate() != null
                        && job.getEndDate().isAfter(job.getMaxEndDate()))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        job -> (int) DAYS.between(job.getMaxEndDate(), job.getEndDate()))
                .asConstraint(new ConstraintInfo(MAX_END_DATE, MAX_END_DATE,
                        "Don't end a maintenance job after it is due.",
                        MaintenanceScheduleConstraintGroup.DEADLINES));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint beforeIdealEndDate(ConstraintFactory constraintFactory) {
        // Early maintenance is expensive because the sooner maintenance is done, the sooner it needs to happen again.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getIdealEndDate() != null
                        && job.getEndDate().isBefore(job.getIdealEndDate()))
                .penalize(HardMediumSoftScore.ofSoft(1),
                        job -> (int) DAYS.between(job.getEndDate(), job.getIdealEndDate()))
                .asConstraint(new ConstraintInfo(BEFORE_IDEAL_END_DATE, BEFORE_IDEAL_END_DATE,
                        "Early maintenance is expensive because it needs to happen again sooner.",
                        MaintenanceScheduleConstraintGroup.PREFERENCES));
    }

    Constraint afterIdealEndDate(ConstraintFactory constraintFactory) {
        // Late maintenance is risky because delays can push it over the due date.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getIdealEndDate() != null
                        && job.getEndDate().isAfter(job.getIdealEndDate()))
                .penalize(HardMediumSoftScore.ofSoft(1_000_000),
                        job -> (int) DAYS.between(job.getIdealEndDate(), job.getEndDate()))
                .asConstraint(new ConstraintInfo(AFTER_IDEAL_END_DATE, AFTER_IDEAL_END_DATE,
                        "Late maintenance is risky because delays can push it over the due date.",
                        MaintenanceScheduleConstraintGroup.PREFERENCES));
    }

    Constraint tagConflict(ConstraintFactory constraintFactory) {
        // Avoid overlapping maintenance jobs with the same tag (for example road maintenance in the same area).
        return constraintFactory
                .forEachUniquePair(Job.class,
                        overlapping(Job::getStartDate, Job::getEndDate),
                        Joiners.containingAnyOf(Job::getTags))
                .penalize(HardMediumSoftScore.ofSoft(1_000),
                        (job1, job2) -> {
                            Set<String> intersection = new HashSet<>(job1.getTags());
                            intersection.retainAll(job2.getTags());
                            long overlap = DAYS.between(
                                    job1.getStartDate().isAfter(job2.getStartDate())
                                            ? job1.getStartDate()
                                            : job2.getStartDate(),
                                    job1.getEndDate().isBefore(job2.getEndDate())
                                            ? job1.getEndDate()
                                            : job2.getEndDate());
                            return (int) (intersection.size() * overlap);
                        })
                .asConstraint(new ConstraintInfo(TAG_CONFLICT, TAG_CONFLICT,
                        "Avoid overlapping maintenance jobs with the same tag.",
                        MaintenanceScheduleConstraintGroup.CONFLICT_AVOIDANCE));
    }
}
