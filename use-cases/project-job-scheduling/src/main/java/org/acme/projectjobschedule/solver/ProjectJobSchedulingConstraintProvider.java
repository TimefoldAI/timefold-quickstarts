package org.acme.projectjobschedule.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.ProjectJobScheduleConstraintProperties;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.domain.justification.ProjectJobScheduleJustification.MakespanJustification;
import org.acme.projectjobschedule.domain.justification.ProjectJobScheduleJustification.NonRenewableResourceOverCapacityJustification;
import org.acme.projectjobschedule.domain.justification.ProjectJobScheduleJustification.ProjectDelayJustification;
import org.acme.projectjobschedule.domain.justification.ProjectJobScheduleJustification.RenewableResourceOverCapacityJustification;

public class ProjectJobSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                nonRenewableResourceCapacity(constraintFactory),
                renewableResourceCapacity(constraintFactory),

                // Medium constraints
                totalProjectDelay(constraintFactory),

                // Soft constraints
                totalMakespan(constraintFactory)
        };
    }

    /**
     * A non-renewable resource is consumed for good, so its capacity limits the total consumption over
     * the whole schedule, whenever the consuming jobs run.
     */
    public Constraint nonRenewableResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ResourceRequirement.class)
                .filter(requirement -> !requirement.isResourceRenewable())
                .join(Allocation.class,
                        Joiners.equal(ResourceRequirement::executionMode, Allocation::getExecutionMode))
                .groupBy((requirement, allocation) -> requirement.resource(),
                        ConstraintCollectors.sum((requirement, allocation) -> requirement.requirement()))
                .filter((resource, requirements) -> requirements > resource.capacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (resource, requirements) -> requirements - resource.capacity())
                .justifyWith((resource, requirements,
                        score) -> NonRenewableResourceOverCapacityJustification.of(resource, requirements))
                .asConstraint(new ConstraintInfo(
                        ProjectJobScheduleConstraintProperties.NON_RENEWABLE_RESOURCE_CAPACITY,
                        ProjectJobScheduleConstraintProperties.NON_RENEWABLE_RESOURCE_CAPACITY,
                        "The total use of each non-renewable resource must not exceed its capacity.",
                        ProjectJobScheduleConstraintGroup.RESOURCE_CAPACITY));
    }

    /**
     * A renewable resource is freed again once a job releases it, so its capacity limits the
     * consumption per day: only the jobs busy on that day compete for it.
     */
    public Constraint renewableResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ResourceRequirement.class)
                .filter(ResourceRequirement::isResourceRenewable)
                .join(Allocation.class,
                        Joiners.equal(ResourceRequirement::executionMode, Allocation::getExecutionMode))
                .flattenLast(Allocation::getBusyDates)
                .groupBy((requirement, date) -> requirement.resource(),
                        (requirement, date) -> date,
                        ConstraintCollectors.sum((requirement, date) -> requirement.requirement()))
                .filter((resource, date, totalRequirement) -> totalRequirement > resource.capacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (resource, date, totalRequirement) -> totalRequirement - resource.capacity())
                .justifyWith((resource, date, totalRequirement,
                        score) -> RenewableResourceOverCapacityJustification.of(resource, date, totalRequirement))
                .asConstraint(new ConstraintInfo(
                        ProjectJobScheduleConstraintProperties.RENEWABLE_RESOURCE_CAPACITY,
                        ProjectJobScheduleConstraintProperties.RENEWABLE_RESOURCE_CAPACITY,
                        "The use of each renewable resource at any given time must not exceed its capacity.",
                        ProjectJobScheduleConstraintGroup.RESOURCE_CAPACITY));
    }

    /**
     * A project ends when its sink job ends, so the delay of a project is what its sink job runs past
     * the project's critical path end date.
     */
    public Constraint totalProjectDelay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Allocation.class)
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .filter(allocation -> allocation.getProjectDelay() > 0)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, Allocation::getProjectDelay)
                .justifyWith((allocation, score) -> ProjectDelayJustification.of(allocation))
                .asConstraint(new ConstraintInfo(ProjectJobScheduleConstraintProperties.TOTAL_PROJECT_DELAY,
                        ProjectJobScheduleConstraintProperties.TOTAL_PROJECT_DELAY,
                        "Minimize the total delay across all projects.",
                        ProjectJobScheduleConstraintGroup.PROJECT_DELAY));
    }

    /**
     * Tie-breaker below the project delay: of two schedules with the same total delay, prefer the one
     * whose last project ends earlier.
     */
    public Constraint totalMakespan(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Allocation.class)
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .groupBy(ConstraintCollectors.max(Allocation::getEndDate))
                .penalize(HardMediumSoftScore.ONE_SOFT, maxEndDate -> maxEndDate)
                .justifyWith((maxEndDate, score) -> MakespanJustification.of(maxEndDate))
                .asConstraint(new ConstraintInfo(ProjectJobScheduleConstraintProperties.TOTAL_MAKESPAN,
                        ProjectJobScheduleConstraintProperties.TOTAL_MAKESPAN,
                        "Minimize the overall project completion time.",
                        ProjectJobScheduleConstraintGroup.MAKESPAN));
    }
}
