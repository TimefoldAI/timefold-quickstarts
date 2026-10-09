package org.acme.facilitylocation.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the facility location problem submitted in the input dataset.")
public record FacilityPlanInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_FACILITIES, title = "Facilities",
                format = DataFormat.Values.NUMBER,
                description = "The number of candidate facilities submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "30", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "30") }) int facilities,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_CONSUMERS, title = "Consumers",
                format = DataFormat.Values.NUMBER,
                description = "The number of consumers submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "60", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "60") }) int consumers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_CAPACITY,
                title = "Total capacity", format = DataFormat.Values.NUMBER,
                description = "The cumulative capacity of every candidate facility in the input dataset.",
                type = SchemaType.INTEGER, examples = "4500", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "4500") }) long totalCapacity,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_DEMAND, title = "Total demand",
                format = DataFormat.Values.NUMBER,
                description = "The cumulative demand of every consumer in the input dataset.",
                type = SchemaType.INTEGER, examples = "900", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "900") }) long totalDemand)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_FACILITIES = "facilities";
    public static final String INPUT_METRIC_CONSUMERS = "consumers";
    public static final String INPUT_METRIC_TOTAL_CAPACITY = "totalCapacity";
    public static final String INPUT_METRIC_TOTAL_DEMAND = "totalDemand";

    public FacilityPlanInputMetrics {
        if (facilities < 0 || consumers < 0 || totalCapacity < 0 || totalDemand < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were facilities (%d), consumers (%d), totalCapacity (%d), totalDemand (%d)."
                            .formatted(facilities, consumers, totalCapacity, totalDemand));
        }
    }
}
