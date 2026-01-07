package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import ai.timefold.solver.core.api.score.stream.Joiners;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Vehicle;

import java.util.function.Function;

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
                // Added the join with the last visit to make sure constraint is triggered by potential changes on the visit,
                // even though it should not be necessary as the Vehicle.arrivalTime shadow var should trigger the constraint?
                .join(Visit.class,
                Joiners.equal(Function.identity(), Visit::getVehicle),
                Joiners.filtering((vehicle,visit) -> (visit.isAssignedAsLast() && visit.getVehicle().equals(vehicle))))
                .filter((vehicle, lastVisit) -> vehicle.isShiftFinishedAfterMaxEndTime())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        (vehicle, lastVisit) -> vehicle.getShiftFinishedDelayInMinutes())
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
