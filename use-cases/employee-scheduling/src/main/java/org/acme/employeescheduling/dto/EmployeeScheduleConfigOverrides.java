package org.acme.employeescheduling.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.employeescheduling.solver.EmployeeSchedulingConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeScheduleConfigOverrides(
        @ConstraintReference(EmployeeSchedulingConstraintProvider.UNDESIRED_DAY_FOR_EMPLOYEE) @Schema(
                description = "Soft weight of the undesired day for employee constraint.") Long undesiredDayForEmployeeWeight,
        @ConstraintReference(EmployeeSchedulingConstraintProvider.DESIRED_DAY_FOR_EMPLOYEE) @Schema(
                description = "Soft weight of the desired day for employee constraint.") Long desiredDayForEmployeeWeight,
        @ConstraintReference(EmployeeSchedulingConstraintProvider.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS) @Schema(
                description = "Soft weight of the balance employee shift assignments constraint.") Long balanceEmployeeShiftAssignmentsWeight)
        implements
            ModelConfigOverrides {

    public EmployeeScheduleConfigOverrides {
        undesiredDayForEmployeeWeight =
                undesiredDayForEmployeeWeight != null && undesiredDayForEmployeeWeight < 0L ? 0L
                        : undesiredDayForEmployeeWeight;
        desiredDayForEmployeeWeight =
                desiredDayForEmployeeWeight != null && desiredDayForEmployeeWeight < 0L ? 0L : desiredDayForEmployeeWeight;
        balanceEmployeeShiftAssignmentsWeight =
                balanceEmployeeShiftAssignmentsWeight != null && balanceEmployeeShiftAssignmentsWeight < 0L ? 0L
                        : balanceEmployeeShiftAssignmentsWeight;
    }

    public EmployeeScheduleConfigOverrides() {
        this(1L, 1L, 1L);
    }

    public EmployeeScheduleConfigOverrides withUndesiredDayForEmployeeWeight(Long undesiredDayForEmployeeWeight) {
        return new EmployeeScheduleConfigOverrides(undesiredDayForEmployeeWeight, desiredDayForEmployeeWeight,
                balanceEmployeeShiftAssignmentsWeight);
    }

    public EmployeeScheduleConfigOverrides withDesiredDayForEmployeeWeight(Long desiredDayForEmployeeWeight) {
        return new EmployeeScheduleConfigOverrides(undesiredDayForEmployeeWeight, desiredDayForEmployeeWeight,
                balanceEmployeeShiftAssignmentsWeight);
    }

    public EmployeeScheduleConfigOverrides withBalanceEmployeeShiftAssignmentsWeight(
            Long balanceEmployeeShiftAssignmentsWeight) {
        return new EmployeeScheduleConfigOverrides(undesiredDayForEmployeeWeight, desiredDayForEmployeeWeight,
                balanceEmployeeShiftAssignmentsWeight);
    }
}
