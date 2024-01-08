package org.acme.vehiclerouting.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record VehicleCapacityJustification(String vehicleId, int capacity, int demand,
        String description) implements ConstraintJustification {

    public VehicleCapacityJustification(String vehicleId, int capacity, int demand) {
        this(vehicleId, capacity, demand, "Vehicle '%s' exceeded its max capacity by %s."
                .formatted(vehicleId, demand - capacity));
    }
}
