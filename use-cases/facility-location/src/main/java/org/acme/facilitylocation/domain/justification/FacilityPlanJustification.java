package org.acme.facilitylocation.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every facility location justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a facility location constraint was matched.",
        oneOf = {
                // Hard constraints
                FacilityPlanJustification.FacilityCapacityJustification.class,

                // Soft constraints
                FacilityPlanJustification.FacilitySetupCostJustification.class,
                FacilityPlanJustification.DistanceFromFacilityJustification.class
        })
public interface FacilityPlanJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    @Schema(allOf = { FacilityPlanJustification.class })
    record FacilityCapacityJustification(String facility, long capacity, long assignedDemand, int consumerCount)
            implements
                FacilityPlanJustification {

        public static FacilityCapacityJustification of(Facility facility, long assignedDemand) {
            return new FacilityCapacityJustification(facility.getId(), facility.getCapacity(), assignedDemand,
                    facility.getConsumers().size());
        }

        @Override
        public String getDescription() {
            return "Facility '%s' serves %d consumer(s) demanding %d in total, which exceeds its capacity of %d by %d."
                    .formatted(facility, consumerCount, assignedDemand, capacity, assignedDemand - capacity);
        }
    }

    @Schema(allOf = { FacilityPlanJustification.class })
    record FacilitySetupCostJustification(String facility, long setupCost, int consumerCount)
            implements
                FacilityPlanJustification {

        public static FacilitySetupCostJustification of(Facility facility, int consumerCount) {
            return new FacilitySetupCostJustification(facility.getId(), facility.getSetupCost(), consumerCount);
        }

        @Override
        public String getDescription() {
            return "Facility '%s' is used by %d consumer(s), so its setup cost of %d has to be paid."
                    .formatted(facility, consumerCount, setupCost);
        }
    }

    @Schema(allOf = { FacilityPlanJustification.class })
    record DistanceFromFacilityJustification(String consumer, String facility, long distanceInMeters)
            implements
                FacilityPlanJustification {

        public static DistanceFromFacilityJustification of(Consumer consumer) {
            return new DistanceFromFacilityJustification(consumer.getId(), consumer.getFacility().getId(),
                    consumer.distanceFromFacility());
        }

        @Override
        public String getDescription() {
            return "Consumer '%s' is %d meter(s) away from its facility '%s'."
                    .formatted(consumer, distanceInMeters, facility);
        }
    }
}
