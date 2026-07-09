package org.acme.vehiclerouting.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the vehicle routing solution produced for this dataset.")
public record VehicleRoutingOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_VISITS, title = "Assigned visits",
                format = DataFormat.Values.NUMBER,
                description = "The number of visits assigned to a vehicle in this solution.",
                type = SchemaType.INTEGER, example = "55", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "55") }) int assignedVisits,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_VISITS, title = "Unassigned visits",
                format = DataFormat.Values.NUMBER,
                description = "The number of visits left without a vehicle in this solution.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int unassignedVisits,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_VEHICLES, title = "Used vehicles",
                format = DataFormat.Values.NUMBER,
                description = "The number of vehicles with at least one assigned visit in this solution.",
                type = SchemaType.INTEGER, example = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int usedVehicles,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_DRIVING_TIME_SECONDS,
                title = "Total driving time (s)",
                format = DataFormat.Values.NUMBER,
                description = "The total driving time in seconds across all vehicles in this solution.",
                type = SchemaType.INTEGER, example = "18000", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "18000") }) long totalDrivingTimeSeconds)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_VISITS = "assignedVisits";
    public static final String TOTAL_UNASSIGNED_VISITS = "unassignedVisits";
    public static final String TOTAL_USED_VEHICLES = "usedVehicles";
    public static final String TOTAL_DRIVING_TIME_SECONDS = "totalDrivingTimeSeconds";

    public VehicleRoutingOutputMetrics {
        if (assignedVisits < 0 || unassignedVisits < 0 || usedVehicles < 0 || totalDrivingTimeSeconds < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public VehicleRoutingOutputMetrics withAssignedVisits(int assignedVisits) {
        return new VehicleRoutingOutputMetrics(assignedVisits, unassignedVisits, usedVehicles, totalDrivingTimeSeconds);
    }

    public VehicleRoutingOutputMetrics withUnassignedVisits(int unassignedVisits) {
        return new VehicleRoutingOutputMetrics(assignedVisits, unassignedVisits, usedVehicles, totalDrivingTimeSeconds);
    }

    public VehicleRoutingOutputMetrics withUsedVehicles(int usedVehicles) {
        return new VehicleRoutingOutputMetrics(assignedVisits, unassignedVisits, usedVehicles, totalDrivingTimeSeconds);
    }

    public VehicleRoutingOutputMetrics withTotalDrivingTimeSeconds(long totalDrivingTimeSeconds) {
        return new VehicleRoutingOutputMetrics(assignedVisits, unassignedVisits, usedVehicles, totalDrivingTimeSeconds);
    }
}
