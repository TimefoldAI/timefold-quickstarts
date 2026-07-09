package org.acme.employeescheduling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the employee scheduling solution produced for this schedule.")
public record EmployeeScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_SHIFTS, title = "Assigned shifts",
                format = DataFormat.Values.NUMBER,
                description = "The number of shifts assigned to an employee in this schedule.",
                type = SchemaType.INTEGER, example = "50", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "50") }) int totalAssignedShifts,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_SHIFTS,
                title = "Unassigned shifts", format = DataFormat.Values.NUMBER,
                description = "The number of shifts left without an employee in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedShifts,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_EMPLOYEES,
                title = "Used employees", format = DataFormat.Values.NUMBER,
                description = "The number of distinct employees assigned to at least one shift in this schedule.",
                type = SchemaType.INTEGER, example = "15", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "15") }) int totalUsedEmployees)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_SHIFTS = "totalAssignedShifts";
    public static final String TOTAL_UNASSIGNED_SHIFTS = "totalUnassignedShifts";
    public static final String TOTAL_USED_EMPLOYEES = "totalUsedEmployees";

    public EmployeeScheduleOutputMetrics {
        if (totalAssignedShifts < 0 || totalUnassignedShifts < 0 || totalUsedEmployees < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public EmployeeScheduleOutputMetrics withTotalAssignedShifts(int totalAssignedShifts) {
        return new EmployeeScheduleOutputMetrics(totalAssignedShifts, totalUnassignedShifts, totalUsedEmployees);
    }

    public EmployeeScheduleOutputMetrics withTotalUnassignedShifts(int totalUnassignedShifts) {
        return new EmployeeScheduleOutputMetrics(totalAssignedShifts, totalUnassignedShifts, totalUsedEmployees);
    }

    public EmployeeScheduleOutputMetrics withTotalUsedEmployees(int totalUsedEmployees) {
        return new EmployeeScheduleOutputMetrics(totalAssignedShifts, totalUnassignedShifts, totalUsedEmployees);
    }
}
