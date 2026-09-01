package org.acme.facilitylocation.domain;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/**
 * A geographical position, used both for facilities and for consumers.
 */
public record Location(double latitude, double longitude) {

    // Approximate Metric Equivalents for Degrees. At the equator for longitude and for latitude anywhere,
    // the following approximations are valid: 1° = 111 km (or 60 nautical miles) 0.1° = 11.1 km.
    public static final double METERS_PER_DEGREE = 111_000;

    /**
     * @return the air distance to {@code other}, in meters
     */
    public long getDistanceTo(Location other) {
        double latitudeDiff = other.latitude - this.latitude;
        double longitudeDiff = other.longitude - this.longitude;
        return (long) ceil(sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff) * METERS_PER_DEGREE);
    }

    @Override
    public String toString() {
        return String.format("[%.4fN, %.4fE]", latitude, longitude);
    }
}
