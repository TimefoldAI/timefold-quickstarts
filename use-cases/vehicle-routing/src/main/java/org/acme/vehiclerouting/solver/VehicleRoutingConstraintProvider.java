package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.Vehicle;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                vehicleCapacity(factory),
                serviceFinishedAfterMaxEndTime(factory),
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
                .asConstraint("vehicleCapacity");
    }

    protected Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Customer.class)
                .filter(Customer::isServiceFinishedAfterMaxEndTime)
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        Customer::getServiceFinishedDelayInMinutes)
                .asConstraint("serviceFinishedAfterMaxEndTime");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .asConstraint("minimizeTravelTime");
    }
}
