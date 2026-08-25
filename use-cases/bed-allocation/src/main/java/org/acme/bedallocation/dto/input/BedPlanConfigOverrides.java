package org.acme.bedallocation.dto.input;

import jakarta.validation.constraints.Min;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.bedallocation.domain.BedPlanConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BedPlanConfigOverrides(
        @ConstraintReference(BedPlanConstraintProperties.PREFERRED_MAXIMUM_ROOM_CAPACITY) @Schema(
                description = "Soft weight of the preferredMaximumRoomCapacity constraint.") @Min(0) Long preferredMaximumRoomCapacityWeight,
        @ConstraintReference(BedPlanConstraintProperties.DEPARTMENT_SPECIALTY) @Schema(
                description = "Soft weight of the departmentSpecialty constraint.") @Min(0) Long departmentSpecialtyWeight,
        @ConstraintReference(BedPlanConstraintProperties.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY) @Schema(
                description = "Soft weight of the departmentSpecialtyNotFirstPriority constraint.") @Min(0) Long departmentSpecialtyNotFirstPriorityWeight,
        @ConstraintReference(BedPlanConstraintProperties.PREFERRED_PATIENT_EQUIPMENT) @Schema(
                description = "Soft weight of the preferredPatientEquipment constraint.") @Min(0) Long preferredPatientEquipmentWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public BedPlanConfigOverrides() {
        this(null, null, null, null);
    }
}
