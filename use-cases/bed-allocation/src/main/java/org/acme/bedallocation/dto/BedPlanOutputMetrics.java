package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the bed allocation solution produced for this schedule.")
public record BedPlanOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_STAYS, title = "Assigned stays",
                format = DataFormat.Values.NUMBER, description = "The number of stays assigned to a bed in this schedule.",
                type = SchemaType.INTEGER, examples = "150", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "150") }) int totalAssignedStays,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_STAYS, title = "Unassigned stays",
                format = DataFormat.Values.NUMBER, description = "The number of stays left without a bed in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedStays,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROOMS, title = "Used rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rooms used by at least one stay in this schedule.",
                type = SchemaType.INTEGER, examples = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int totalUsedRooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_BEDS, title = "Used beds",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct beds used by at least one stay in this schedule.",
                type = SchemaType.INTEGER, examples = "13", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "13") }) int totalUsedBeds)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_STAYS = "totalAssignedStays";
    public static final String TOTAL_UNASSIGNED_STAYS = "totalUnassignedStays";
    public static final String TOTAL_USED_ROOMS = "totalUsedRooms";
    public static final String TOTAL_USED_BEDS = "totalUsedBeds";

    public BedPlanOutputMetrics {
        if (totalAssignedStays < 0 || totalUnassignedStays < 0 || totalUsedRooms < 0 || totalUsedBeds < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedStays (%d), totalUnassignedStays (%d), totalUsedRooms (%d), totalUsedBeds (%d)."
                            .formatted(totalAssignedStays, totalUnassignedStays, totalUsedRooms, totalUsedBeds));
        }
    }
}
