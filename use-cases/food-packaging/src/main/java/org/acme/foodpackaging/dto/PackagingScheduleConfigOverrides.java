package org.acme.foodpackaging.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.foodpackaging.solver.FoodPackagingConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Medium and soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PackagingScheduleConfigOverrides(
        @ConstraintReference(FoodPackagingConstraintProvider.IDEAL_END_DATE_TIME) @Schema(
                description = "Medium weight of the ideal end date time constraint.") Long idealEndDateTimeWeight,
        @ConstraintReference(FoodPackagingConstraintProvider.MAXIMIZE_JOBS_ASSIGNED) @Schema(
                description = "Medium weight of the maximize jobs assigned constraint.") Long maximizeJobsAssignedWeight,
        @ConstraintReference(FoodPackagingConstraintProvider.MINIMIZE_MAKESPAN) @Schema(
                description = "Soft weight of the minimize makespan constraint.") Long minimizeMakespanWeight)
        implements
            ModelConfigOverrides {

    public PackagingScheduleConfigOverrides {
        idealEndDateTimeWeight = idealEndDateTimeWeight != null && idealEndDateTimeWeight < 0L ? 0L : idealEndDateTimeWeight;
        maximizeJobsAssignedWeight =
                maximizeJobsAssignedWeight != null && maximizeJobsAssignedWeight < 0L ? 0L : maximizeJobsAssignedWeight;
        minimizeMakespanWeight =
                minimizeMakespanWeight != null && minimizeMakespanWeight < 0L ? 0L : minimizeMakespanWeight;
    }

    public PackagingScheduleConfigOverrides() {
        this(1L, 1L, 1L);
    }

    public PackagingScheduleConfigOverrides withIdealEndDateTimeWeight(Long idealEndDateTimeWeight) {
        return new PackagingScheduleConfigOverrides(idealEndDateTimeWeight, maximizeJobsAssignedWeight,
                minimizeMakespanWeight);
    }

    public PackagingScheduleConfigOverrides withMaximizeJobsAssignedWeight(Long maximizeJobsAssignedWeight) {
        return new PackagingScheduleConfigOverrides(idealEndDateTimeWeight, maximizeJobsAssignedWeight,
                minimizeMakespanWeight);
    }

    public PackagingScheduleConfigOverrides withMinimizeMakespanWeight(Long minimizeMakespanWeight) {
        return new PackagingScheduleConfigOverrides(idealEndDateTimeWeight, maximizeJobsAssignedWeight,
                minimizeMakespanWeight);
    }
}
