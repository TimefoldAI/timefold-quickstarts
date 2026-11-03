package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlanConstraintProperties;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.justification.VehicleRoutePlanJustification.ServiceFinishedAfterMaxEndTimeJustification;
import org.acme.vehiclerouting.domain.justification.VehicleRoutePlanJustification.TravelTimeJustification;
import org.acme.vehiclerouting.domain.justification.VehicleRoutePlanJustification.VehicleCapacityJustification;
import org.acme.vehiclerouting.domain.justification.VehicleRoutePlanJustification.VisitNotAssignedJustification;

public class VehicleRoutePlanConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard constraints
                vehicleCapacity(factory),
                serviceFinishedAfterMaxEndTime(factory),

                // Medium constraints
                maximizeVisitsAssigned(factory),

                // Soft constraints
                minimizeTravelTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    public Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .justifyWith((vehicle, score) -> VehicleCapacityJustification.of(vehicle))
                .asConstraint(new ConstraintInfo(VehicleRoutePlanConstraintProperties.VEHICLE_CAPACITY,
                        VehicleRoutePlanConstraintProperties.VEHICLE_CAPACITY,
                        "The total demand of all visits assigned to a vehicle must not exceed its capacity.",
                        VehicleRoutePlanConstraintGroup.VEHICLE_CAPACITY));
    }

    public Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isServiceFinishedAfterMaxEndTime)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        Visit::getServiceFinishedDelayInMinutes)
                .justifyWith((visit, score) -> ServiceFinishedAfterMaxEndTimeJustification.of(visit))
                .asConstraint(
                        new ConstraintInfo(VehicleRoutePlanConstraintProperties.SERVICE_FINISHED_AFTER_MAX_END_TIME,
                                VehicleRoutePlanConstraintProperties.SERVICE_FINISHED_AFTER_MAX_END_TIME,
                                "A visit must be serviced before its maximum end time.",
                                VehicleRoutePlanConstraintGroup.TIME_WINDOWS));
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    public Constraint maximizeVisitsAssigned(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Visit.class)
                .filter(visit -> visit.getVehicle() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, visit -> visit.getServiceDuration().toMinutes())
                .justifyWith((visit, score) -> VisitNotAssignedJustification.of(visit))
                .asConstraint(new ConstraintInfo(VehicleRoutePlanConstraintProperties.MAXIMIZE_VISITS_ASSIGNED,
                        VehicleRoutePlanConstraintProperties.MAXIMIZE_VISITS_ASSIGNED,
                        "As many visits as possible should be assigned to a vehicle.",
                        VehicleRoutePlanConstraintGroup.VISIT_ASSIGNMENT));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .justifyWith((vehicle, score) -> TravelTimeJustification.of(vehicle))
                .asConstraint(new ConstraintInfo(VehicleRoutePlanConstraintProperties.MINIMIZE_TRAVEL_TIME,
                        VehicleRoutePlanConstraintProperties.MINIMIZE_TRAVEL_TIME,
                        "Minimize the total travel time of all vehicles.",
                        VehicleRoutePlanConstraintGroup.TRAVEL_TIME));
    }
}
