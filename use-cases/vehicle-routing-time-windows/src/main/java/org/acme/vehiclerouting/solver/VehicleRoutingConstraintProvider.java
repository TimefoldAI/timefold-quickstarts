package org.acme.vehiclerouting.solver;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                totalDistance(factory),
                serviceFinishedAfterDueTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint serviceFinishedAfterDueTime(ConstraintFactory factory) {
        return factory.forEach(Customer.class)
                .filter(Customer::isServiceFinishedAfterDueTime)
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        Customer::getServiceFinishedDelayInMinutes)
                .asConstraint("serviceFinishedAfterDueTime");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint totalDistance(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDistanceMeters)
                .asConstraint("distanceFromPreviousStandstill");
    }
}
