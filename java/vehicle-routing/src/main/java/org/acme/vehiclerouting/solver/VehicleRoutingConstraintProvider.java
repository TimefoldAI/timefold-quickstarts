package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Vehicle;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String SERVICE_FINISHED_AFTER_MAX_END_TIME = "serviceFinishedAfterMaxEndTime";
    public static final String SHIFT_FINISHED_AFTER_MAX_END_TIME = "shiftFinishedAfterMaxEndTime";
    public static final String MINIMIZE_TRAVEL_TIME = "minimizeTravelTime";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                vehicleCapacity(factory),
                serviceFinishedAfterMaxEndTime(factory),
                shiftFinishedAfterMaxEndTime(factory),
                minimizeTravelTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint(VEHICLE_CAPACITY);
    }

    protected Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isServiceFinishedAfterMaxEndTime)
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        Visit::getServiceFinishedDelayInMinutes)
                .asConstraint(SERVICE_FINISHED_AFTER_MAX_END_TIME);
    }

    protected Constraint shiftFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(Vehicle::isShiftFinishedAfterMaxEndTime)
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        Vehicle::getShiftFinishedDelayInMinutes)
                .asConstraint(SHIFT_FINISHED_AFTER_MAX_END_TIME);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .asConstraint(MINIMIZE_TRAVEL_TIME);
    }
}
