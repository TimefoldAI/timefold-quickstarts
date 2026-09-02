package org.acme.vehiclerouting.domain.justification;

import java.time.OffsetDateTime;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every vehicle routing justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a vehicle routing constraint was matched.",
        oneOf = {
                // Hard constraints
                VehicleRoutePlanJustification.VehicleCapacityJustification.class,
                VehicleRoutePlanJustification.ServiceFinishedAfterMaxEndTimeJustification.class,

                // Medium constraints
                VehicleRoutePlanJustification.VisitNotAssignedJustification.class,

                // Soft constraints
                VehicleRoutePlanJustification.TravelTimeJustification.class
        })
public interface VehicleRoutePlanJustification extends ModelConstraintJustification {

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

    @Schema(description = "The visits on one vehicle's route demand more than that vehicle can carry.",
            allOf = { VehicleRoutePlanJustification.class })
    record VehicleCapacityJustification(
            @Schema(description = "The id of the overloaded vehicle.") String vehicle,
            @Schema(description = "How much the vehicle can carry.") int capacity,
            @Schema(description = "The demand of every visit on its route added up.") int totalDemand,
            @Schema(description = "How much demand does not fit.") int excessDemand)
            implements
                VehicleRoutePlanJustification {

        public static VehicleCapacityJustification of(Vehicle vehicle) {
            return new VehicleCapacityJustification(vehicle.getId(), vehicle.getCapacity(), vehicle.getTotalDemand(),
                    vehicle.getTotalDemand() - vehicle.getCapacity());
        }

        @Override
        public String getDescription() {
            return "Vehicle '%s' carries a demand of %d, which is %d over its capacity of %d."
                    .formatted(vehicle, totalDemand, excessDemand, capacity);
        }
    }

    @Schema(description = "Servicing a visit finishes after the end of the time window it accepts a vehicle in.",
            allOf = { VehicleRoutePlanJustification.class })
    record ServiceFinishedAfterMaxEndTimeJustification(
            @Schema(description = "The id of the visit.") String visit,
            @Schema(description = "The id of the vehicle servicing it.") String vehicle,
            @Schema(description = "The latest time servicing may finish.") OffsetDateTime maxEndTime,
            @Schema(description = "The time servicing actually finishes.") OffsetDateTime serviceFinishedTime,
            @Schema(description = "How many minutes too late servicing finishes.") long delayInMinutes)
            implements
                VehicleRoutePlanJustification {

        public static ServiceFinishedAfterMaxEndTimeJustification of(Visit visit) {
            Vehicle vehicle = visit.getVehicle();
            return new ServiceFinishedAfterMaxEndTimeJustification(visit.getId(),
                    vehicle == null ? null : vehicle.getId(), visit.getMaxEndTime(),
                    visit.getArrivalTime().plus(visit.getServiceDuration()),
                    visit.getServiceFinishedDelayInMinutes());
        }

        @Override
        public String getDescription() {
            return "Visit '%s' is serviced by vehicle '%s' until %s, which is %d minute(s) after its maximum end time %s."
                    .formatted(visit, vehicle, serviceFinishedTime, delayInMinutes, maxEndTime);
        }
    }

    @Schema(description = "A visit is on no vehicle's route, so nobody services it.",
            allOf = { VehicleRoutePlanJustification.class })
    record VisitNotAssignedJustification(
            @Schema(description = "The id of the unassigned visit.") String visit,
            @Schema(description = "How many minutes of servicing the visit needs.") long serviceDurationInMinutes)
            implements
                VehicleRoutePlanJustification {

        public static VisitNotAssignedJustification of(Visit visit) {
            return new VisitNotAssignedJustification(visit.getId(), visit.getServiceDuration().toMinutes());
        }

        @Override
        public String getDescription() {
            return "Visit '%s' is assigned to no vehicle, leaving %d minute(s) of servicing undone."
                    .formatted(visit, serviceDurationInMinutes);
        }
    }

    @Schema(description = "A vehicle spends time driving its route.",
            allOf = { VehicleRoutePlanJustification.class })
    record TravelTimeJustification(
            @Schema(description = "The id of the vehicle.") String vehicle,
            @Schema(description = "How many visits are on its route.") int visitCount,
            @Schema(description = "How many seconds it drives, home location to home location.") long drivingTimeSeconds)
            implements
                VehicleRoutePlanJustification {

        public static TravelTimeJustification of(Vehicle vehicle) {
            return new TravelTimeJustification(vehicle.getId(), vehicle.getVisits().size(),
                    vehicle.getTotalDrivingTimeSeconds());
        }

        @Override
        public String getDescription() {
            return "Vehicle '%s' drives %d minute(s) to service %d visit(s)."
                    .formatted(vehicle, drivingTimeSeconds / 60, visitCount);
        }
    }
}
