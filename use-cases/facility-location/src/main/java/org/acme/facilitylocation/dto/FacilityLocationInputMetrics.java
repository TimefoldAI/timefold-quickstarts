package org.acme.facilitylocation.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the facility location problem submitted in the input dataset.")
public record FacilityLocationInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_FACILITIES, title = "Facilities",
                format = DataFormat.Values.NUMBER,
                description = "The number of candidate facilities submitted in the input dataset.",
                type = SchemaType.NUMBER, example = "50", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "50") }) int facilities,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_CONSUMERS, title = "Consumers",
                format = DataFormat.Values.NUMBER,
                description = "The number of consumers submitted in the input dataset.",
                type = SchemaType.NUMBER, example = "150", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "150") }) int consumers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_DEMAND, title = "Total demand",
                format = DataFormat.Values.NUMBER,
                description = "The combined demand of all consumers submitted in the input dataset.",
                type = SchemaType.NUMBER, example = "7400", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "7400") }) long totalDemand,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_CAPACITY, title = "Total capacity",
                format = DataFormat.Values.NUMBER,
                description = "The combined capacity of all candidate facilities submitted in the input dataset.",
                type = SchemaType.NUMBER, example = "12000", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "12000") }) long totalCapacity,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TOTAL_POTENTIAL_SETUP_COST,
                title = "Total potential setup cost", format = DataFormat.Values.NUMBER,
                description = "The combined setup cost of opening every candidate facility in the input dataset.",
                type = SchemaType.NUMBER, example = "65000", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "65000") }) long totalPotentialSetupCost)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_FACILITIES = "facilities";
    public static final String INPUT_METRIC_CONSUMERS = "consumers";
    public static final String INPUT_METRIC_TOTAL_DEMAND = "totalDemand";
    public static final String INPUT_METRIC_TOTAL_CAPACITY = "totalCapacity";
    public static final String INPUT_METRIC_TOTAL_POTENTIAL_SETUP_COST = "totalPotentialSetupCost";

    public FacilityLocationInputMetrics {
        if (facilities < 0 || consumers < 0 || totalDemand < 0 || totalCapacity < 0 || totalPotentialSetupCost < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public FacilityLocationInputMetrics withFacilities(int facilities) {
        return new FacilityLocationInputMetrics(facilities, consumers, totalDemand, totalCapacity,
                totalPotentialSetupCost);
    }

    public FacilityLocationInputMetrics withConsumers(int consumers) {
        return new FacilityLocationInputMetrics(facilities, consumers, totalDemand, totalCapacity,
                totalPotentialSetupCost);
    }

    public FacilityLocationInputMetrics withTotalDemand(long totalDemand) {
        return new FacilityLocationInputMetrics(facilities, consumers, totalDemand, totalCapacity,
                totalPotentialSetupCost);
    }

    public FacilityLocationInputMetrics withTotalCapacity(long totalCapacity) {
        return new FacilityLocationInputMetrics(facilities, consumers, totalDemand, totalCapacity,
                totalPotentialSetupCost);
    }

    public FacilityLocationInputMetrics withTotalPotentialSetupCost(long totalPotentialSetupCost) {
        return new FacilityLocationInputMetrics(facilities, consumers, totalDemand, totalCapacity,
                totalPotentialSetupCost);
    }
}
