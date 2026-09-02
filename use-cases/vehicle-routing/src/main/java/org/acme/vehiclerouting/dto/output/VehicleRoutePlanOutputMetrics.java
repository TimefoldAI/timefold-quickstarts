package org.acme.vehiclerouting.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the route plan produced for this dataset.")
public record VehicleRoutePlanOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_VISITS,
                title = "Assigned visits", format = DataFormat.Values.NUMBER,
                description = "The number of visits assigned to a vehicle's route in this plan.",
                type = SchemaType.INTEGER, examples = "74", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "74") }) int totalAssignedVisits,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_VISITS,
                title = "Unassigned visits", format = DataFormat.Values.NUMBER,
                description = "The number of visits left out of every vehicle's route in this plan.",
                type = SchemaType.INTEGER, examples = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int totalUnassignedVisits,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_VEHICLES, title = "Used vehicles",
                format = DataFormat.Values.NUMBER,
                description = "The number of vehicles that drive at least one visit in this plan.",
                type = SchemaType.INTEGER, examples = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int totalUsedVehicles,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_DRIVING_TIME_SECONDS,
                title = "Total driving time", format = DataFormat.Values.NUMBER,
                description = "The driving time of every vehicle's whole route added up, in seconds, including the "
                        + "drive back to its home location.",
                type = SchemaType.INTEGER, examples = "37260", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "37260") }) long totalDrivingTimeSeconds)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_VISITS = "totalAssignedVisits";
    public static final String TOTAL_UNASSIGNED_VISITS = "totalUnassignedVisits";
    public static final String TOTAL_USED_VEHICLES = "totalUsedVehicles";
    public static final String TOTAL_DRIVING_TIME_SECONDS = "totalDrivingTimeSeconds";

    public VehicleRoutePlanOutputMetrics {
        if (totalAssignedVisits < 0 || totalUnassignedVisits < 0 || totalUsedVehicles < 0
                || totalDrivingTimeSeconds < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedVisits (%d), totalUnassignedVisits (%d), totalUsedVehicles (%d), totalDrivingTimeSeconds (%d)."
                            .formatted(totalAssignedVisits, totalUnassignedVisits, totalUsedVehicles,
                                    totalDrivingTimeSeconds));
        }
    }
}
