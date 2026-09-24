package org.acme.maintenancescheduling.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the maintenance scheduling problem submitted in the input dataset.")
public record MaintenanceScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_JOBS, title = "Jobs",
                format = DataFormat.Values.NUMBER, description = "The number of jobs submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "28", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "28") }) int jobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_CREWS, title = "Crews",
                format = DataFormat.Values.NUMBER, description = "The number of crews submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "4", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "4") }) int crews,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_WORKDAYS, title = "Workdays",
                format = DataFormat.Values.NUMBER,
                description = "The number of workdays in the submitted work calendar, weekends excluded.",
                type = SchemaType.INTEGER, examples = "60", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "60") }) int workdays)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_JOBS = "jobs";
    public static final String INPUT_METRIC_CREWS = "crews";
    public static final String INPUT_METRIC_WORKDAYS = "workdays";

    public MaintenanceScheduleInputMetrics {
        if (jobs < 0 || crews < 0 || workdays < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were jobs (%d), crews (%d), workdays (%d)."
                            .formatted(jobs, crews, workdays));
        }
    }
}
