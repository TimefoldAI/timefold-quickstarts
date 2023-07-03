package org.acme.vehiclerouting.domain.geo;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.vehiclerouting.domain.Location;

public interface DistanceCalculator {

    /**
     * Calculate the distance between {@code from} and {@code to} in seconds of driving time.
     *
     * @param from starting location
     * @param to target location
     * @return driving time in seconds
     */
    long calculateDistance(Location from, Location to);

    /**
     * Bulk calculation of distance.
     * Typically, much more scalable than {@link #calculateDistance(Location, Location)} iteratively.
     *
     * @param fromLocations never null
     * @param toLocations never null
     * @return never null
     */
    default Map<Location, Map<Location, Long>> calculateBulkDistance(
            Collection<Location> fromLocations,
            Collection<Location> toLocations) {
        return fromLocations.stream().collect(Collectors.toMap(
                Function.identity(),
                from -> toLocations.stream().collect(Collectors.toMap(
                        Function.identity(),
                        to -> calculateDistance(from, to)))));
    }

    /**
     * Calculate distance matrix for the given list of locations and assign distance maps accordingly.
     *
     * @param locations locations list
     */
    default void initDistanceMaps(Collection<Location> locations) {
        Map<Location, Map<Location, Long>> distanceMatrix = calculateBulkDistance(locations, locations);
        locations.forEach(location -> location.setDrivingTimeSecondsMap(distanceMatrix.get(location)));
    }
}
