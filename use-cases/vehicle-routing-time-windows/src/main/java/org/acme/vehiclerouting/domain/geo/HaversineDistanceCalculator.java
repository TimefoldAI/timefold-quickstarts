package org.acme.vehiclerouting.domain.geo;

import org.acme.vehiclerouting.domain.Location;

public class HaversineDistanceCalculator implements DistanceCalculator {

    private static final int EARTH_RADIUS_IN_KM = 6371;
    private static final int TWICE_EARTH_RADIUS_IN_KM = 2 * EARTH_RADIUS_IN_KM;
    public static final int AVERAGE_SPEED_KMPH = 60;

    public static long kilometersToDrivingSeconds(double kilometers) {
        return Math.round(kilometers / AVERAGE_SPEED_KMPH * 3_600);
    }

    @Override
    public long calculateDistance(Location from, Location to) {
        if (from.equals(to)) {
            return 0L;
        }

        CartesianCoordinate fromCartesian = locationToCartesian(from);
        CartesianCoordinate toCartesian = locationToCartesian(to);
        return calculateDistance(fromCartesian, toCartesian);
    }

    private long calculateDistance(CartesianCoordinate from, CartesianCoordinate to) {
        if (from.equals(to)) {
            return 0L;
        }

        double dX = from.x - to.x;
        double dY = from.y - to.y;
        double dZ = from.z - to.z;
        double r = Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
        return kilometersToDrivingSeconds(TWICE_EARTH_RADIUS_IN_KM * Math.asin(r));
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
