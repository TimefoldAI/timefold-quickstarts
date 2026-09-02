package org.acme.vehiclerouting.domain;

import java.util.Map;

/**
 * A geographic coordinate, together with the driving time from here to every other location of the
 * same route plan.
 * <p>
 * Deliberately not a record and deliberately without an {@code equals}/{@code hashCode} override:
 * two visits may sit on the exact same coordinate, and each of them still needs its own row in the
 * driving time matrix, so identity is the right notion of equality here. It is also the cheap one -
 * this class is a hash key on every driving time lookup, of which the solver does millions.
 */
public class Location {

    private final double latitude;
    private final double longitude;

    private Map<Location, Long> drivingTimeSeconds;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<Location, Long> getDrivingTimeSeconds() {
        return drivingTimeSeconds;
    }

    /**
     * Set the driving time map (in seconds).
     *
     * @param drivingTimeSeconds a map containing driving time from here to other locations
     */
    public void setDrivingTimeSeconds(Map<Location, Long> drivingTimeSeconds) {
        this.drivingTimeSeconds = drivingTimeSeconds;
    }

    /**
     * Driving time to the given location in seconds.
     *
     * @param location other location
     * @return driving time in seconds
     */
    public long getDrivingTimeTo(Location location) {
        return drivingTimeSeconds.get(location);
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }

}
