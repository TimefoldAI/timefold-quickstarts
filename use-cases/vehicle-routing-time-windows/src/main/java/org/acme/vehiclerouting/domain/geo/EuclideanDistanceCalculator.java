package org.acme.vehiclerouting.domain.geo;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

import org.acme.vehiclerouting.domain.Location;

public class EuclideanDistanceCalculator implements DistanceCalculator {

    // Approximate Metric Equivalents for Degrees. At the equator for longitude and for latitude anywhere,
    // the following approximations are valid: 1° = 111 km (or 60 nautical miles) 0.1° = 11.1 km.
    public static final long METERS_PER_DEGREE = 111_000;

    @Override
    public long calculateDistance(Location from, Location to) {
        if (from.equals(to)) {
            return 0L;
        }
        double latitudeDiff = to.getLatitude() - from.getLatitude();
        double longitudeDiff = to.getLongitude() - from.getLongitude();
        return (long) ceil(sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff) * METERS_PER_DEGREE);
    }
}
