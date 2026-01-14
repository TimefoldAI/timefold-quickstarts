package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Vehicle;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String MAXIMIZE_VISITS_ASSIGNED = "maximizeVisitsAssigned";
    public static final String SERVICE_FINISHED_AFTER_MAX_END_TIME = "serviceFinishedAfterMaxEndTime";
    public static final String MINIMIZE_TRAVEL_TIME = "minimizeTravelTime";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard
                vehicleCapacity(factory),
                serviceFinishedAfterMaxEndTime(factory),

                // Medium
                maximizeVisitsAssigned(factory),

                // Soft
                minimizeTravelTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint(VEHICLE_CAPACITY);
    }

    protected Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isServiceFinishedAfterMaxEndTime)
                .penalizeLong(HardMediumSoftLongScore.ONE_HARD,
                        Visit::getServiceFinishedDelayInMinutes)
                .asConstraint(SERVICE_FINISHED_AFTER_MAX_END_TIME);
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    protected Constraint maximizeVisitsAssigned(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Visit.class)
                .filter(v -> v.getVehicle() == null)
                .penalizeLong(HardMediumSoftLongScore.ONE_MEDIUM, v-> v.getServiceDuration().toMinutes())
                .asConstraint(MAXIMIZE_VISITS_ASSIGNED);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardMediumSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .asConstraint(MINIMIZE_TRAVEL_TIME);
    }
}
