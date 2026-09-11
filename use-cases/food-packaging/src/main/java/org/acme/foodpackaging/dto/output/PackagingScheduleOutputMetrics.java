package org.acme.foodpackaging.dto.output;

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
                description = "The number of jobs scheduled on a production line in this schedule.",
                type = SchemaType.INTEGER, examples = "100", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "100") }) int totalAssignedJobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_JOBS,
                title = "Unassigned jobs", format = DataFormat.Values.NUMBER,
                description = "The number of jobs left without a production line in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedJobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_LINES, title = "Used lines",
                format = DataFormat.Values.NUMBER,
                description = "The number of production lines producing at least one job in this schedule.",
                type = SchemaType.INTEGER, examples = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int totalUsedLines,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_CLEANING_MINUTES,
                title = "Cleaning minutes", format = DataFormat.Values.NUMBER,
                description = "The total number of minutes the lines spend on changeover cleaning in this schedule.",
                type = SchemaType.INTEGER, examples = "1200", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "1200") }) long totalCleaningMinutes)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_JOBS = "totalAssignedJobs";
    public static final String TOTAL_UNASSIGNED_JOBS = "totalUnassignedJobs";
    public static final String TOTAL_USED_LINES = "totalUsedLines";
    public static final String TOTAL_CLEANING_MINUTES = "totalCleaningMinutes";

    public PackagingScheduleOutputMetrics {
        if (totalAssignedJobs < 0 || totalUnassignedJobs < 0 || totalUsedLines < 0 || totalCleaningMinutes < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedJobs (%d), totalUnassignedJobs (%d), totalUsedLines (%d), totalCleaningMinutes (%d)."
                            .formatted(totalAssignedJobs, totalUnassignedJobs, totalUsedLines, totalCleaningMinutes));
        }
    }
}
