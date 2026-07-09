package org.acme.projectjobschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the project job scheduling solution produced for this schedule.")
public record ProjectJobScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_MAKESPAN, title = "Total makespan",
                format = DataFormat.Values.NUMBER,
                description = "The latest end date across all projects in this schedule, in days.",
                type = SchemaType.INTEGER, example = "40", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "40") }) int totalMakespan,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_PROJECT_DELAY, title = "Total project delay",
                format = DataFormat.Values.NUMBER,
                description = "The sum of positive project delays beyond the critical path end date, in days.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalProjectDelay,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_SCHEDULED_ALLOCATIONS,
                title = "Scheduled allocations", format = DataFormat.Values.NUMBER,
                description = "The number of allocations assigned an execution mode in this schedule.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int totalScheduledAllocations,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNSCHEDULED_ALLOCATIONS,
                title = "Unscheduled allocations", format = DataFormat.Values.NUMBER,
                description = "The number of allocations left without an execution mode in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnscheduledAllocations)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_MAKESPAN = "totalMakespan";
    public static final String TOTAL_PROJECT_DELAY = "totalProjectDelay";
    public static final String TOTAL_SCHEDULED_ALLOCATIONS = "totalScheduledAllocations";
    public static final String TOTAL_UNSCHEDULED_ALLOCATIONS = "totalUnscheduledAllocations";

    public ProjectJobScheduleOutputMetrics {
        if (totalMakespan < 0 || totalProjectDelay < 0 || totalScheduledAllocations < 0
                || totalUnscheduledAllocations < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public ProjectJobScheduleOutputMetrics withTotalMakespan(int totalMakespan) {
        return new ProjectJobScheduleOutputMetrics(totalMakespan, totalProjectDelay, totalScheduledAllocations,
                totalUnscheduledAllocations);
    }

    public ProjectJobScheduleOutputMetrics withTotalProjectDelay(int totalProjectDelay) {
        return new ProjectJobScheduleOutputMetrics(totalMakespan, totalProjectDelay, totalScheduledAllocations,
                totalUnscheduledAllocations);
    }

    public ProjectJobScheduleOutputMetrics withTotalScheduledAllocations(int totalScheduledAllocations) {
        return new ProjectJobScheduleOutputMetrics(totalMakespan, totalProjectDelay, totalScheduledAllocations,
                totalUnscheduledAllocations);
    }

    public ProjectJobScheduleOutputMetrics withTotalUnscheduledAllocations(int totalUnscheduledAllocations) {
        return new ProjectJobScheduleOutputMetrics(totalMakespan, totalProjectDelay, totalScheduledAllocations,
                totalUnscheduledAllocations);
    }
}
