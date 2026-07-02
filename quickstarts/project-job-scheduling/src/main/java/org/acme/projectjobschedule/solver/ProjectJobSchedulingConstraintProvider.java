package org.acme.projectjobschedule.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.ResourceRequirement;

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

    protected Constraint nonRenewableResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ResourceRequirement.class)
                .filter(resource -> !resource.isResourceRenewable())
                .join(Allocation.class,
                        Joiners.equal(ResourceRequirement::getExecutionMode, Allocation::getExecutionMode))
                .groupBy((requirement, allocation) -> requirement.getResource(),
                        ConstraintCollectors.sum((requirement, allocation) -> requirement.getRequirement()))
                .filter((resource, requirements) -> requirements > resource.getCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (resource, requirements) -> requirements - resource.getCapacity())
                .asConstraint("Non-renewable resource capacity");
    }

    protected Constraint renewableResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ResourceRequirement.class)
                .filter(ResourceRequirement::isResourceRenewable)
                .join(Allocation.class,
                        Joiners.equal(ResourceRequirement::getExecutionMode, Allocation::getExecutionMode))
                .flattenLast(Allocation::getBusyDates)
                .groupBy((resourceReq, date) -> resourceReq.getResource(),
                        (resourceReq, date) -> date,
                        ConstraintCollectors.sum((resourceReq, date) -> resourceReq.getRequirement()))
                .filter((resourceReq, date, totalRequirement) -> totalRequirement > resourceReq.getCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (resourceReq, date, totalRequirement) -> totalRequirement - resourceReq.getCapacity())
                .asConstraint("Renewable resource capacity");
    }

    protected Constraint totalProjectDelay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Allocation.class)
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .filter(allocation -> allocation.getProjectDelay() > 0)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, Allocation::getProjectDelay)
                .asConstraint("Total project delay");
    }

    protected Constraint totalMakespan(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Allocation.class)
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .filter(allocation -> allocation.getEndDate() != null)
                .groupBy(ConstraintCollectors.max(Allocation::getEndDate))
                .penalize(HardMediumSoftScore.ONE_SOFT, maxEndDate -> maxEndDate)
                .asConstraint("Total makespan");
    }

}
