package org.acme.projectjobschedule.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the project job scheduling problem submitted in the input dataset.")
public record ProjectJobScheduleInputMetrics(
        @Schema(title = "Allocations", format = DataFormat.Values.NUMBER,
                description = "The number of job allocations submitted in the input dataset.",
                examples = "24", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int allocations,
        @Schema(title = "Jobs", format = DataFormat.Values.NUMBER,
                description = "The number of jobs submitted in the input dataset.",
                examples = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int jobs,
        @Schema(title = "Projects", format = DataFormat.Values.NUMBER,
                description = "The number of projects submitted in the input dataset.",
                examples = "2", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "2") }) int projects,
        @Schema(title = "Resources", format = DataFormat.Values.NUMBER,
                description = "The number of resources submitted in the input dataset.",
                examples = "7", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "7") }) int resources)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public ProjectJobScheduleInputMetrics {
        if (allocations < 0 || jobs < 0 || projects < 0 || resources < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were allocations (%d), jobs (%d), projects (%d), resources (%d)."
                            .formatted(allocations, jobs, projects, resources));
        }
    }
}
