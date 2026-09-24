package org.acme.maintenancescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;
import static java.time.temporal.ChronoUnit.DAYS;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceScheduleConstraintProperties;
import org.acme.maintenancescheduling.domain.justification.MaintenanceScheduleJustification.AfterIdealEndDateJustification;
import org.acme.maintenancescheduling.domain.justification.MaintenanceScheduleJustification.BeforeIdealEndDateJustification;
import org.acme.maintenancescheduling.domain.justification.MaintenanceScheduleJustification.CrewConflictJustification;
import org.acme.maintenancescheduling.domain.justification.MaintenanceScheduleJustification.MaxEndDateJustification;
import org.acme.maintenancescheduling.domain.justification.MaintenanceScheduleJustification.MinStartDateJustification;
import org.acme.maintenancescheduling.domain.justification.MaintenanceScheduleJustification.TagConflictJustification;

public class MaintenanceScheduleConstraintProvider implements ConstraintProvider {

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

    public Constraint crewConflict(ConstraintFactory constraintFactory) {
        // A crew can do at most one maintenance job at the same time.
        return constraintFactory
                .forEachUniquePair(Job.class,
                        equal(Job::getCrew),
                        overlapping(Job::getStartDate, Job::getEndDate))
                .penalize(HardSoftScore.ONE_HARD, Job::calculateOverlapInDays)
                .justifyWith((job, otherJob, score) -> CrewConflictJustification.of(job, otherJob))
                .asConstraint(new ConstraintInfo(MaintenanceScheduleConstraintProperties.CREW_CONFLICT,
                        MaintenanceScheduleConstraintProperties.CREW_CONFLICT,
                        "A crew must not work on two maintenance jobs at the same time.",
                        MaintenanceScheduleConstraintGroup.CREW_CONFLICTS));
    }

    public Constraint minStartDate(ConstraintFactory constraintFactory) {
        // Don't start a maintenance job before it's ready to start.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getMinStartDate() != null
                        && job.getStartDate().isBefore(job.getMinStartDate()))
                .penalize(HardSoftScore.ONE_HARD,
                        job -> DAYS.between(job.getStartDate(), job.getMinStartDate()))
                .justifyWith((job, score) -> MinStartDateJustification.of(job))
                .asConstraint(new ConstraintInfo(MaintenanceScheduleConstraintProperties.MIN_START_DATE,
                        MaintenanceScheduleConstraintProperties.MIN_START_DATE,
                        "A maintenance job must not start before the date it is ready to be worked on.",
                        MaintenanceScheduleConstraintGroup.MAINTENANCE_WINDOW));
    }

    public Constraint maxEndDate(ConstraintFactory constraintFactory) {
        // Don't end a maintenance job after it's due.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getMaxEndDate() != null
                        && job.getEndDate().isAfter(job.getMaxEndDate()))
                .penalize(HardSoftScore.ONE_HARD,
                        job -> DAYS.between(job.getMaxEndDate(), job.getEndDate()))
                .justifyWith((job, score) -> MaxEndDateJustification.of(job))
                .asConstraint(new ConstraintInfo(MaintenanceScheduleConstraintProperties.MAX_END_DATE,
                        MaintenanceScheduleConstraintProperties.MAX_END_DATE,
                        "A maintenance job must not end after the date it is due.",
                        MaintenanceScheduleConstraintGroup.MAINTENANCE_WINDOW));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint beforeIdealEndDate(ConstraintFactory constraintFactory) {
        // Early maintenance is expensive because the sooner maintenance is done, the sooner it needs to happen again.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getIdealEndDate() != null
                        && job.getEndDate().isBefore(job.getIdealEndDate()))
                .penalize(HardSoftScore.ofSoft(1),
                        job -> DAYS.between(job.getEndDate(), job.getIdealEndDate()))
                .justifyWith((job, score) -> BeforeIdealEndDateJustification.of(job))
                .asConstraint(new ConstraintInfo(MaintenanceScheduleConstraintProperties.BEFORE_IDEAL_END_DATE,
                        MaintenanceScheduleConstraintProperties.BEFORE_IDEAL_END_DATE,
                        "A maintenance job should not end long before its ideal end date, because maintenance done "
                                + "too early needs to happen again sooner.",
                        MaintenanceScheduleConstraintGroup.MAINTENANCE_TIMING));
    }

    public Constraint afterIdealEndDate(ConstraintFactory constraintFactory) {
        // Late maintenance is risky because delays can push it over the due date.
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getIdealEndDate() != null
                        && job.getEndDate().isAfter(job.getIdealEndDate()))
                .penalize(HardSoftScore.ofSoft(1_000_000),
                        job -> DAYS.between(job.getIdealEndDate(), job.getEndDate()))
                .justifyWith((job, score) -> AfterIdealEndDateJustification.of(job))
                .asConstraint(new ConstraintInfo(MaintenanceScheduleConstraintProperties.AFTER_IDEAL_END_DATE,
                        MaintenanceScheduleConstraintProperties.AFTER_IDEAL_END_DATE,
                        "A maintenance job should not end after its ideal end date, because any further delay "
                                + "pushes it over its due date.",
                        MaintenanceScheduleConstraintGroup.MAINTENANCE_TIMING));
    }

    public Constraint tagConflict(ConstraintFactory constraintFactory) {
        // Avoid overlapping maintenance jobs with the same tag (for example road maintenance in the same area).
        return constraintFactory
                .forEachUniquePair(Job.class,
                        overlapping(Job::getStartDate, Job::getEndDate),
                        Joiners.containingAnyOf(Job::getTags))
                .penalize(HardSoftScore.ofSoft(1_000),
                        (job, otherJob) -> job.calculateSharedTags(otherJob).size()
                                * job.calculateOverlapInDays(otherJob))
                .justifyWith((job, otherJob, score) -> TagConflictJustification.of(job, otherJob))
                .asConstraint(new ConstraintInfo(MaintenanceScheduleConstraintProperties.TAG_CONFLICT,
                        MaintenanceScheduleConstraintProperties.TAG_CONFLICT,
                        "Maintenance jobs that share a tag should not be worked on at the same time.",
                        MaintenanceScheduleConstraintGroup.TAG_CONFLICTS));
    }
}
