package org.acme.projectjobschedule.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the project job schedule produced for this dataset.")
public record ProjectJobScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_ALLOCATIONS,
                title = "Scheduled jobs", format = DataFormat.Values.NUMBER,
                description = "The number of jobs that got both an execution mode and a delay in this schedule.",
                type = SchemaType.INTEGER, examples = "24", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int totalAssignedAllocations,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_ALLOCATIONS,
                title = "Unscheduled jobs", format = DataFormat.Values.NUMBER,
                description = "The number of jobs left without an execution mode or a delay in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedAllocations,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_PROJECT_DELAY, title = "Project delay",
                format = DataFormat.Values.NUMBER,
                description = "The total number of days by which the projects end past their critical path.",
                type = SchemaType.INTEGER, examples = "12", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "12") }) int totalProjectDelay,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_MAKESPAN, title = "Makespan",
                format = DataFormat.Values.NUMBER,
                description = "The number of days between the start of the schedule and the last project ending.",
                type = SchemaType.INTEGER, examples = "27", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "27") }) int totalMakespan)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_ALLOCATIONS = "totalAssignedAllocations";
    public static final String TOTAL_UNASSIGNED_ALLOCATIONS = "totalUnassignedAllocations";
    public static final String TOTAL_PROJECT_DELAY = "totalProjectDelay";
    public static final String TOTAL_MAKESPAN = "totalMakespan";

    public ProjectJobScheduleOutputMetrics {
        if (totalAssignedAllocations < 0 || totalUnassignedAllocations < 0 || totalProjectDelay < 0
                || totalMakespan < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedAllocations (%d), totalUnassignedAllocations (%d), totalProjectDelay (%d), totalMakespan (%d)."
                            .formatted(totalAssignedAllocations, totalUnassignedAllocations, totalProjectDelay,
                                    totalMakespan));
        }
    }
}
