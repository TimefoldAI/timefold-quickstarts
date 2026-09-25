package org.acme.vehiclerouting.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the vehicle routing problem submitted in the input dataset.")
public record VehicleRoutePlanInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_VISITS, title = "Visits",
                format = DataFormat.Values.NUMBER, description = "The number of visits submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "77", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "77") }) int visits,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_VEHICLES, title = "Vehicles",
                format = DataFormat.Values.NUMBER,
                description = "The number of vehicles submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int vehicles,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_DEMAND,
                title = "Total demand", format = DataFormat.Values.NUMBER,
                description = "The demand of every submitted visit added up.",
                type = SchemaType.INTEGER, examples = "116", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "116") }) int totalDemand,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_CAPACITY,
                title = "Total capacity", format = DataFormat.Values.NUMBER,
                description = "The capacity of every submitted vehicle added up. Below the total demand, some visits "
                        + "can never be assigned.",
                type = SchemaType.INTEGER, examples = "180", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "180") }) int totalCapacity)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_VISITS = "visits";
    public static final String INPUT_METRIC_VEHICLES = "vehicles";
    public static final String INPUT_METRIC_TOTAL_DEMAND = "totalDemand";
    public static final String INPUT_METRIC_TOTAL_CAPACITY = "totalCapacity";

    public VehicleRoutePlanInputMetrics {
        if (visits < 0 || vehicles < 0 || totalDemand < 0 || totalCapacity < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were visits (%d), vehicles (%d), totalDemand (%d), totalCapacity (%d)."
                            .formatted(visits, vehicles, totalDemand, totalCapacity));
        }
    }
}
