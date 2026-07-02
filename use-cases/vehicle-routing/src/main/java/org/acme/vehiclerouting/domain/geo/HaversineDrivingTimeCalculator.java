package org.acme.vehiclerouting.domain.geo;

import org.acme.vehiclerouting.domain.Location;

/**
 * Calculates the driving time (in seconds) between two locations by calculating their Haversine distance in meters
 * assuming average speed {@link #AVERAGE_SPEED_KMPH}.
 */
public final class HaversineDrivingTimeCalculator implements DrivingTimeCalculator {

    private static final HaversineDrivingTimeCalculator INSTANCE = new HaversineDrivingTimeCalculator();

    public static final int AVERAGE_SPEED_KMPH = 50;

    private static final int EARTH_RADIUS_IN_M = 6371000;
    private static final int TWICE_EARTH_RADIUS_IN_M = 2 * EARTH_RADIUS_IN_M;

    static long metersToDrivingSeconds(long meters) {
        return Math.round((double) meters / AVERAGE_SPEED_KMPH * 3.6);
    }

    public static synchronized HaversineDrivingTimeCalculator getInstance() {
        return INSTANCE;
    }

    private HaversineDrivingTimeCalculator() {
    }

    @Override
    public long calculateDrivingTime(Location from, Location to) {
        if (from.equals(to)) {
            return 0L;
        }

        CartesianCoordinate fromCartesian = locationToCartesian(from);
        CartesianCoordinate toCartesian = locationToCartesian(to);
        return metersToDrivingSeconds(calculateDistance(fromCartesian, toCartesian));
    }

    private long calculateDistance(CartesianCoordinate from, CartesianCoordinate to) {
        if (from.equals(to)) {
            return 0L;
        }

        double dX = from.x - to.x;
        double dY = from.y - to.y;
        double dZ = from.z - to.z;
        double r = Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
        return Math.round(TWICE_EARTH_RADIUS_IN_M * Math.asin(r));
    }

    private CartesianCoordinate locationToCartesian(Location location) {
        double latitudeInRads = Math.toRadians(location.getLatitude());
        double longitudeInRads = Math.toRadians(location.getLongitude());
        // Cartesian coordinates, normalized for a sphere of diameter 1.0
        double cartesianX = 0.5 * Math.cos(latitudeInRads) * Math.sin(longitudeInRads);
        double cartesianY = 0.5 * Math.cos(latitudeInRads) * Math.cos(longitudeInRads);
        double cartesianZ = 0.5 * Math.sin(latitudeInRads);
        return new CartesianCoordinate(cartesianX, cartesianY, cartesianZ);
    }

    private record CartesianCoordinate(double x, double y, double z) {

    }
}
