package org.acme.employeescheduling.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the employee schedule produced for this dataset.")
public record EmployeeScheduleOutputMetrics(
        @Schema(title = "Assigned shifts", format = DataFormat.Values.NUMBER,
                description = "The number of shifts that got an employee in this schedule.",
                examples = "120", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "120") }) int totalAssignedShifts,
        @Schema(title = "Unassigned shifts", format = DataFormat.Values.NUMBER,
                description = "The number of shifts left without an employee in this schedule.",
                examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedShifts,
        @Schema(title = "Shifts on desired days", format = DataFormat.Values.NUMBER,
                description = "The number of assigned shifts that fall on a day their employee wants to work.",
                examples = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int shiftsOnDesiredDays,
        @Schema(title = "Shifts on undesired days", format = DataFormat.Values.NUMBER,
                description = "The number of assigned shifts that fall on a day their employee would rather not work.",
                examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int shiftsOnUndesiredDays)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public EmployeeScheduleOutputMetrics {
        if (totalAssignedShifts < 0 || totalUnassignedShifts < 0 || shiftsOnDesiredDays < 0
                || shiftsOnUndesiredDays < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedShifts (%d), totalUnassignedShifts (%d), shiftsOnDesiredDays (%d), shiftsOnUndesiredDays (%d)."
                            .formatted(totalAssignedShifts, totalUnassignedShifts, shiftsOnDesiredDays,
                                    shiftsOnUndesiredDays));
        }
    }
}
