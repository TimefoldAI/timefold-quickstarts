package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String SERVICE_FINISHED_AFTER_MAX_END_TIME = "serviceFinishedAfterMaxEndTime";
    public static final String MAXIMIZE_VISITS_ASSIGNED = "maximizeVisitsAssigned";
    public static final String MINIMIZE_TRAVEL_TIME = "minimizeTravelTime";

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

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint(new ConstraintInfo(VEHICLE_CAPACITY, VEHICLE_CAPACITY,
                        "A vehicle must not carry more demand than its capacity.",
                        VehicleRoutingConstraintGroup.CAPACITY));
    }

    protected Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isServiceFinishedAfterMaxEndTime)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        Visit::getServiceFinishedDelayInMinutes)
                .asConstraint(new ConstraintInfo(SERVICE_FINISHED_AFTER_MAX_END_TIME, SERVICE_FINISHED_AFTER_MAX_END_TIME,
                        "A visit must be serviced before its time window closes.",
                        VehicleRoutingConstraintGroup.TIME_WINDOWS));
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    protected Constraint maximizeVisitsAssigned(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Visit.class)
                .filter(visit -> visit.getVehicle() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, visit -> visit.getServiceDuration().toMinutes())
                .asConstraint(new ConstraintInfo(MAXIMIZE_VISITS_ASSIGNED, MAXIMIZE_VISITS_ASSIGNED,
                        "Every visit should ideally be assigned to a vehicle.",
                        VehicleRoutingConstraintGroup.VISIT_ASSIGNMENT));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .asConstraint(new ConstraintInfo(MINIMIZE_TRAVEL_TIME, MINIMIZE_TRAVEL_TIME,
                        "Minimize the total driving time across all vehicles.",
                        VehicleRoutingConstraintGroup.TRAVEL_TIME));
    }
}
