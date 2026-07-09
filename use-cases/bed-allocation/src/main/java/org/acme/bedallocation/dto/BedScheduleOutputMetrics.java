package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the bed allocation solution produced for this schedule.")
public record BedScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_STAYS, title = "Assigned stays",
                format = DataFormat.Values.NUMBER,
                description = "The number of stays assigned to a bed in this schedule.",
                type = SchemaType.INTEGER, example = "100", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "100") }) int totalAssignedStays,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_STAYS, title = "Unassigned stays",
                format = DataFormat.Values.NUMBER,
                description = "The number of stays left without a bed in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedStays,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROOMS, title = "Used rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rooms used by at least one stay in this schedule.",
                type = SchemaType.INTEGER, example = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int totalUsedRooms)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_STAYS = "totalAssignedStays";
    public static final String TOTAL_UNASSIGNED_STAYS = "totalUnassignedStays";
    public static final String TOTAL_USED_ROOMS = "totalUsedRooms";

    public BedScheduleOutputMetrics {
        if (totalAssignedStays < 0 || totalUnassignedStays < 0 || totalUsedRooms < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public BedScheduleOutputMetrics withTotalAssignedStays(int totalAssignedStays) {
        return new BedScheduleOutputMetrics(totalAssignedStays, totalUnassignedStays, totalUsedRooms);
    }

    public BedScheduleOutputMetrics withTotalUnassignedStays(int totalUnassignedStays) {
        return new BedScheduleOutputMetrics(totalAssignedStays, totalUnassignedStays, totalUsedRooms);
    }

    public BedScheduleOutputMetrics withTotalUsedRooms(int totalUsedRooms) {
        return new BedScheduleOutputMetrics(totalAssignedStays, totalUnassignedStays, totalUsedRooms);
    }
}
