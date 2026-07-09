package org.acme.facilitylocation.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the facility location solution produced for this schedule.")
public record FacilityLocationOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ACTIVATED_FACILITIES,
                title = "Activated facilities", format = DataFormat.Values.NUMBER,
                description = "The number of facilities that have been assigned at least one consumer in this schedule.",
                type = SchemaType.INTEGER, example = "8", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "8") }) int totalActivatedFacilities,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNUSED_FACILITIES,
                title = "Unused facilities", format = DataFormat.Values.NUMBER,
                description = "The number of candidate facilities left without any consumer in this schedule.",
                type = SchemaType.INTEGER, example = "42", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "42") }) int totalUnusedFacilities,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_SETUP_COST, title = "Total setup cost",
                format = DataFormat.Values.NUMBER,
                description = "The combined setup cost of all activated facilities in this schedule.",
                type = SchemaType.INTEGER, example = "11500", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "11500") }) long totalSetupCost,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_CONSUMERS,
                title = "Assigned consumers", format = DataFormat.Values.NUMBER,
                description = "The number of consumers assigned to a facility in this schedule.",
                type = SchemaType.INTEGER, example = "150", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "150") }) int totalAssignedConsumers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_CONSUMERS,
                title = "Unassigned consumers", format = DataFormat.Values.NUMBER,
                description = "The number of consumers that could not be assigned to a facility in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedConsumers,
        @Schema(name = TOTAL_TRAVEL_DISTANCE_METERS, title = "Total travel distance",
                format = DataFormat.Values.DISTANCE,
                description = "The combined distance (in whole meters) between all assigned consumers and their facility.",
                type = SchemaType.INTEGER, example = "784911", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "6"),
                        @Extension(name = X_TF_EXAMPLE, value = "784.91 km") }) long totalTravelDistanceMeters,
        @Schema(name = AVERAGE_TRAVEL_DISTANCE_METERS_PER_CONSUMER, title = "Average travel distance per consumer",
                format = DataFormat.Values.DISTANCE,
                description = "The average distance (in whole meters) between an assigned consumer and its facility.",
                type = SchemaType.INTEGER, example = "5232", minimum = "0", readOnly = true, nullable = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "7"),
                        @Extension(name = X_TF_EXAMPLE,
                                value = "5.23 km") }) Long averageTravelDistanceMetersPerConsumer,
        @Schema(name = CAPACITY_UTILIZATION_PERCENTAGE, title = "Capacity utilization",
                format = DataFormat.Values.PERCENTAGE,
                description = "The percentage of the activated facilities' capacity consumed by their assigned demand. "
                        + "The higher the percentage, the more tightly the activated facilities are packed.",
                type = SchemaType.NUMBER, example = "82.5", minimum = "0", readOnly = true, nullable = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "8"),
                        @Extension(name = X_TF_EXAMPLE, value = "82.5%") }) Double capacityUtilizationPercentage)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ACTIVATED_FACILITIES = "totalActivatedFacilities";
    public static final String TOTAL_UNUSED_FACILITIES = "totalUnusedFacilities";
    public static final String TOTAL_SETUP_COST = "totalSetupCost";
    public static final String TOTAL_ASSIGNED_CONSUMERS = "totalAssignedConsumers";
    public static final String TOTAL_UNASSIGNED_CONSUMERS = "totalUnassignedConsumers";
    public static final String TOTAL_TRAVEL_DISTANCE_METERS = "totalTravelDistanceMeters";
    public static final String AVERAGE_TRAVEL_DISTANCE_METERS_PER_CONSUMER = "averageTravelDistanceMetersPerConsumer";
    public static final String CAPACITY_UTILIZATION_PERCENTAGE = "capacityUtilizationPercentage";

    public FacilityLocationOutputMetrics {
        if (totalActivatedFacilities < 0 || totalUnusedFacilities < 0 || totalSetupCost < 0 || totalAssignedConsumers < 0
                || totalUnassignedConsumers < 0 || totalTravelDistanceMeters < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public FacilityLocationOutputMetrics withTotalActivatedFacilities(int totalActivatedFacilities) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withTotalUnusedFacilities(int totalUnusedFacilities) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withTotalSetupCost(long totalSetupCost) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withTotalAssignedConsumers(int totalAssignedConsumers) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withTotalUnassignedConsumers(int totalUnassignedConsumers) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withTotalTravelDistanceMeters(long totalTravelDistanceMeters) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withAverageTravelDistanceMetersPerConsumer(
            Long averageTravelDistanceMetersPerConsumer) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    public FacilityLocationOutputMetrics withCapacityUtilizationPercentage(Double capacityUtilizationPercentage) {
        return new FacilityLocationOutputMetrics(totalActivatedFacilities, totalUnusedFacilities, totalSetupCost,
                totalAssignedConsumers, totalUnassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }
}
