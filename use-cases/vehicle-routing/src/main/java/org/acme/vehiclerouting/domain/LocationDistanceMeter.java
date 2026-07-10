package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

public class LocationDistanceMeter implements NearbyDistanceMeter<Visit, LocationAware> {

    @Override
    public double getNearbyDistance(Visit origin, LocationAware destination) {
        return origin.getLocation().getDrivingTimeTo(destination.getLocation());
    }
}
