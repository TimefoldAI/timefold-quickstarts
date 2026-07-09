package org.acme.facilitylocation.domain;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class Location {
    // Approximate Metric Equivalents for Degrees. At the equator for longitude and
    // for latitude anywhere,
    // the following approximations are valid: 1° = 111 km (or 60 nautical miles)
    // 0.1° = 11.1 km.
    public static final double METERS_PER_DEGREE = 111_000;

    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("[%.4fN, %.4fE]", latitude, longitude);
    }

    public long getDistanceTo(Location other) {
        double latitudeDiff = other.latitude - this.latitude;
        double longitudeDiff = other.longitude - this.longitude;
        return (long) ceil(sqrt(latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff) * METERS_PER_DEGREE);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Location min(Location other) {
        return new Location(Math.min(this.latitude, other.latitude), Math.min(this.longitude, other.longitude));
    }

    public Location max(Location other) {
        return new Location(Math.max(this.latitude, other.latitude), Math.max(this.longitude, other.longitude));
    }
}
