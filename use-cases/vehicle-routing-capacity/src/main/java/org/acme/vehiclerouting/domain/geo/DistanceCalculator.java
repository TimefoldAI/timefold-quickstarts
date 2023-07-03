package org.acme.vehiclerouting.domain.geo;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.vehiclerouting.domain.Location;

public interface DistanceCalculator {

    /**
     * Calculate the distance between {@code from} and {@code to} in meters.
     *
     * @param from starting location
     * @param to   target location
     * @return distance in meters
     */
    long calculateDistance(Location from, Location to);

    /**
     * Bulk calculation of distance.
     * Typically, much more scalable than {@link #calculateDistance(Location, Location)} iteratively.
     *
     * @param fromLocations never null
     * @param toLocations   never null
     * @return never null
     */
    default Map<Location, Map<Location, Long>> calculateBulkDistance(
            Collection<Location> fromLocations,
            Collection<Location> toLocations) {
        return fromLocations.stream().collect(Collectors.toMap(
                Function.identity(),
                from -> toLocations.stream().collect(Collectors.toMap(
                        Function.identity(),
                        to -> calculateDistance(from, to)
                ))
        ));
    }

    /**
     * Calculate distance matrix for the given list of locations and assign distance maps accordingly.
     *
     * @param locations
     */
    default void initDistanceMaps(Collection<Location> locations) {
        Map<Location, Map<Location, Long>> distanceMatrix = calculateBulkDistance(locations, locations);
        locations.forEach(location -> location.setDistanceMap(distanceMatrix.get(location)));
    }
}
