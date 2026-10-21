package org.acme.employeescheduling.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.employeescheduling.domain.EmployeeScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = """
        Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. \
        A weight left unset (null) is not overridden here, so the value from the configuration profile \
        (or the constraint's default) applies. This makes it possible to override some weights via the \
        input while leaving others to the configuration profile.""")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeScheduleConfigOverrides(
        @ConstraintReference(EmployeeScheduleConstraintProperties.UNDESIRED_DAY_FOR_EMPLOYEE) @Schema(
                description = "Soft weight of the undesired day for employee constraint.",
                minimum = "0") Long undesiredDayForEmployeeWeight,
        @ConstraintReference(EmployeeScheduleConstraintProperties.DESIRED_DAY_FOR_EMPLOYEE) @Schema(
                description = "Soft weight of the desired day for employee constraint.",
                minimum = "0") Long desiredDayForEmployeeWeight,
        @ConstraintReference(EmployeeScheduleConstraintProperties.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS) @Schema(
                description = "Soft weight of the balance employee shift assignments constraint.",
                minimum = "0") Long balanceEmployeeShiftAssignmentsWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public EmployeeScheduleConfigOverrides() {
        this(null, null, null);
    }
}
