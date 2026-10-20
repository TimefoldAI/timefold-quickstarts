package org.acme.orderpicking.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the picking plan produced for this dataset.")
public record PickingPlanOutputMetrics(
        @Schema(title = "Total travel distance", format = DataFormat.Values.NUMBER,
                description = "The distance in meters all trolleys walk together, each from its start location and back.",
                examples = "600", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "600") }) int totalTravelDistance,
        @Schema(title = "Used trolleys", format = DataFormat.Values.NUMBER,
                description = "The number of trolleys that have at least one pick task in this picking plan.",
                examples = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int usedTrolleys,
        @Schema(title = "Split orders", format = DataFormat.Values.NUMBER,
                description = "The number of orders whose items are picked by more than one trolley.",
                examples = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int splitOrders)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public PickingPlanOutputMetrics {
        if (totalTravelDistance < 0 || usedTrolleys < 0 || splitOrders < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalTravelDistance (%d), usedTrolleys (%d), splitOrders (%d)."
                            .formatted(totalTravelDistance, usedTrolleys, splitOrders));
        }
    }
}
