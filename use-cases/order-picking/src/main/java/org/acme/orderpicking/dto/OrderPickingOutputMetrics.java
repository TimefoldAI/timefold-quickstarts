package org.acme.orderpicking.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the order picking solution produced for this dataset.")
public record OrderPickingOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_PICK_TASKS,
                title = "Assigned pick tasks", format = DataFormat.Values.NUMBER,
                description = "The number of pick tasks assigned to a trolley in this solution.",
                type = SchemaType.INTEGER, example = "40", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "40") }) int totalAssignedPickTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_PICK_TASKS,
                title = "Unassigned pick tasks", format = DataFormat.Values.NUMBER,
                description = "The number of pick tasks left without a trolley in this solution.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedPickTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_TROLLEYS, title = "Used trolleys",
                format = DataFormat.Values.NUMBER,
                description = "The number of trolleys used by at least one pick task in this solution.",
                type = SchemaType.INTEGER, example = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int totalUsedTrolleys,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_DISTANCE_TO_TRAVEL,
                title = "Total distance to travel", format = DataFormat.Values.NUMBER,
                description = "The total distance in meters travelled by all trolleys in this solution.",
                type = SchemaType.INTEGER, example = "1200", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "1200") }) long totalDistanceToTravel)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_PICK_TASKS = "totalAssignedPickTasks";
    public static final String TOTAL_UNASSIGNED_PICK_TASKS = "totalUnassignedPickTasks";
    public static final String TOTAL_USED_TROLLEYS = "totalUsedTrolleys";
    public static final String TOTAL_DISTANCE_TO_TRAVEL = "totalDistanceToTravel";

    public OrderPickingOutputMetrics {
        if (totalAssignedPickTasks < 0 || totalUnassignedPickTasks < 0 || totalUsedTrolleys < 0
                || totalDistanceToTravel < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public OrderPickingOutputMetrics withTotalAssignedPickTasks(int totalAssignedPickTasks) {
        return new OrderPickingOutputMetrics(totalAssignedPickTasks, totalUnassignedPickTasks, totalUsedTrolleys,
                totalDistanceToTravel);
    }

    public OrderPickingOutputMetrics withTotalUnassignedPickTasks(int totalUnassignedPickTasks) {
        return new OrderPickingOutputMetrics(totalAssignedPickTasks, totalUnassignedPickTasks, totalUsedTrolleys,
                totalDistanceToTravel);
    }

    public OrderPickingOutputMetrics withTotalUsedTrolleys(int totalUsedTrolleys) {
        return new OrderPickingOutputMetrics(totalAssignedPickTasks, totalUnassignedPickTasks, totalUsedTrolleys,
                totalDistanceToTravel);
    }

    public OrderPickingOutputMetrics withTotalDistanceToTravel(long totalDistanceToTravel) {
        return new OrderPickingOutputMetrics(totalAssignedPickTasks, totalUnassignedPickTasks, totalUsedTrolleys,
                totalDistanceToTravel);
    }
}
