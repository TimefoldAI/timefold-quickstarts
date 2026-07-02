package org.acme.vehiclerouting.domain.geo;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.vehiclerouting.domain.Location;

public interface DrivingTimeCalculator {

    /**
     * Calculate the driving time between {@code from} and {@code to} in seconds.
     *
     * @param from starting location
     * @param to target location
     * @return driving time in seconds
     */
    long calculateDrivingTime(Location from, Location to);

    /**
     * Bulk calculation of driving time.
     * Typically, much more scalable than {@link #calculateDrivingTime(Location, Location)} iteratively.
     *
     * @param fromLocations never null
     * @param toLocations never null
     * @return never null
     */
    default Map<Location, Map<Location, Long>> calculateBulkDrivingTime(
            Collection<Location> fromLocations,
            Collection<Location> toLocations) {
        return fromLocations.stream().collect(Collectors.toMap(
                Function.identity(),
                from -> toLocations.stream().collect(Collectors.toMap(
                        Function.identity(),
                        to -> calculateDrivingTime(from, to)))));
    }

    /**
     * Calculate driving time matrix for the given list of locations and assign driving time maps accordingly.
     *
     * @param locations locations list
     */
    default void initDrivingTimeMaps(Collection<Location> locations) {
        Map<Location, Map<Location, Long>> drivingTimeMatrix = calculateBulkDrivingTime(locations, locations);
        locations.forEach(location -> location.setDrivingTimeSeconds(drivingTimeMatrix.get(location)));
    }
}
