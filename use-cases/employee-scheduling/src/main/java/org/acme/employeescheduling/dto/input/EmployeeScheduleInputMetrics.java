package org.acme.employeescheduling.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the employee scheduling problem submitted in the input dataset.")
public record EmployeeScheduleInputMetrics(
        @Schema(title = "Employees", format = DataFormat.Values.NUMBER,
                description = "The number of employees submitted in the input dataset.",
                examples = "15", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "15") }) int employees,
        @Schema(title = "Shifts", format = DataFormat.Values.NUMBER,
                description = "The number of shifts submitted in the input dataset.",
                examples = "120", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "120") }) int shifts,
        @Schema(title = "Locations", format = DataFormat.Values.NUMBER,
                description = "The number of distinct locations the shifts take place at.",
                examples = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int locations,
        @Schema(title = "Days", format = DataFormat.Values.NUMBER,
                description = "The number of distinct days the shifts start on.",
                examples = "14", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "14") }) int days)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public EmployeeScheduleInputMetrics {
        if (employees < 0 || shifts < 0 || locations < 0 || days < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were employees (%d), shifts (%d), locations (%d), days (%d)."
                            .formatted(employees, shifts, locations, days));
        }
    }
}
