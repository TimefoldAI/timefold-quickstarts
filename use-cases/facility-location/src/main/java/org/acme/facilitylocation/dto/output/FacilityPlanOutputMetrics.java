package org.acme.facilitylocation.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the facility location solution produced for this plan.")
public record FacilityPlanOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_CONSUMERS,
                title = "Assigned consumers", format = DataFormat.Values.NUMBER,
                description = "The number of consumers served by a facility in this plan.",
                type = SchemaType.INTEGER, examples = "60", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "60") }) int totalAssignedConsumers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_CONSUMERS,
                title = "Unassigned consumers", format = DataFormat.Values.NUMBER,
                description = "The number of consumers left without a facility in this plan.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedConsumers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_FACILITIES,
                title = "Used facilities", format = DataFormat.Values.NUMBER,
                description = "The number of facilities that serve at least one consumer in this plan.",
                type = SchemaType.INTEGER, examples = "9", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "9") }) int totalUsedFacilities,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_SETUP_COST, title = "Total setup cost",
                format = DataFormat.Values.NUMBER,
                description = "The cumulative setup cost of the facilities used in this plan.",
                type = SchemaType.INTEGER, examples = "450000", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "450000") }) long totalSetupCost,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_DISTANCE_IN_METERS,
                title = "Total distance", format = DataFormat.Values.DISTANCE,
                description = "The cumulative distance, in meters, between the consumers and the facility serving them.",
                type = SchemaType.INTEGER, examples = "51000", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "51000") }) long totalDistanceInMeters)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_CONSUMERS = "totalAssignedConsumers";
    public static final String TOTAL_UNASSIGNED_CONSUMERS = "totalUnassignedConsumers";
    public static final String TOTAL_USED_FACILITIES = "totalUsedFacilities";
    public static final String TOTAL_SETUP_COST = "totalSetupCost";
    public static final String TOTAL_DISTANCE_IN_METERS = "totalDistanceInMeters";

    public FacilityPlanOutputMetrics {
        if (totalAssignedConsumers < 0 || totalUnassignedConsumers < 0 || totalUsedFacilities < 0
                || totalSetupCost < 0 || totalDistanceInMeters < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedConsumers (%d), totalUnassignedConsumers (%d), totalUsedFacilities (%d), totalSetupCost (%d), totalDistanceInMeters (%d)."
                            .formatted(totalAssignedConsumers, totalUnassignedConsumers, totalUsedFacilities,
                                    totalSetupCost, totalDistanceInMeters));
        }
    }
}
