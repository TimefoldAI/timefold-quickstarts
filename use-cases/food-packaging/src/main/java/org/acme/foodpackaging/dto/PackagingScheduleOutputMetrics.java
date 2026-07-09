package org.acme.foodpackaging.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the food packaging solution produced for this schedule.")
public record PackagingScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_JOBS, title = "Assigned jobs",
                format = DataFormat.Values.NUMBER,
                description = "The number of jobs assigned to a line in this schedule.",
                type = SchemaType.INTEGER, example = "100", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "100") }) int totalAssignedJobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_JOBS,
                title = "Unassigned jobs", format = DataFormat.Values.NUMBER,
                description = "The number of jobs left without a line in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedJobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_LINES, title = "Used lines",
                format = DataFormat.Values.NUMBER,
                description = "The number of lines processing at least one job in this schedule.",
                type = SchemaType.INTEGER, example = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int totalUsedLines,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = MAKESPAN_MINUTES, title = "Makespan (minutes)",
                format = DataFormat.Values.NUMBER,
                description = "The total production span in minutes, from the earliest line start to the latest job end.",
                type = SchemaType.INTEGER, example = "4320", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "4320") }) int makespanMinutes)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_JOBS = "totalAssignedJobs";
    public static final String TOTAL_UNASSIGNED_JOBS = "totalUnassignedJobs";
    public static final String TOTAL_USED_LINES = "totalUsedLines";
    public static final String MAKESPAN_MINUTES = "makespanMinutes";

    public PackagingScheduleOutputMetrics {
        if (totalAssignedJobs < 0 || totalUnassignedJobs < 0 || totalUsedLines < 0 || makespanMinutes < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public PackagingScheduleOutputMetrics withTotalAssignedJobs(int totalAssignedJobs) {
        return new PackagingScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedLines,
                makespanMinutes);
    }

    public PackagingScheduleOutputMetrics withTotalUnassignedJobs(int totalUnassignedJobs) {
        return new PackagingScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedLines,
                makespanMinutes);
    }

    public PackagingScheduleOutputMetrics withTotalUsedLines(int totalUsedLines) {
        return new PackagingScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedLines,
                makespanMinutes);
    }

    public PackagingScheduleOutputMetrics withMakespanMinutes(int makespanMinutes) {
        return new PackagingScheduleOutputMetrics(totalAssignedJobs, totalUnassignedJobs, totalUsedLines,
                makespanMinutes);
    }
}
