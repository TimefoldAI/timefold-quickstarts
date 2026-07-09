package org.acme.vehiclerouting.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the vehicle routing problem submitted in the input dataset.")
public record VehicleRoutingInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_VEHICLES, title = "Vehicles",
                format = DataFormat.Values.NUMBER,
                description = "The number of vehicles submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "6", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int vehicles,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_VISITS, title = "Visits",
                format = DataFormat.Values.NUMBER,
                description = "The number of visits submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "55", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "55") }) int visits)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_VEHICLES = "vehicles";
    public static final String INPUT_METRIC_VISITS = "visits";

    public VehicleRoutingInputMetrics {
        if (vehicles < 0 || visits < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public VehicleRoutingInputMetrics withVehicles(int vehicles) {
        return new VehicleRoutingInputMetrics(vehicles, visits);
    }

    public VehicleRoutingInputMetrics withVisits(int visits) {
        return new VehicleRoutingInputMetrics(vehicles, visits);
    }
}
