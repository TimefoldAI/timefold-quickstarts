package org.acme.vehiclerouting.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonIdentityInfo(scope = Location.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Location {

    private final long id;
    private final double latitude;
    private final double longitude;
    private Map<Location, Long> distanceMap;

    public Location(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the distance map. Distances are in meters.
     *
     * @param distanceMap a map containing distances from here to other locations
     */
    public void setDistanceMap(Map<Location, Long> distanceMap) {
        this.distanceMap = distanceMap;
    }

    /**
     * Distance to the given location in meters.
     *
     * @param location other location
     * @return distance in meters
     */
    public long getDistanceTo(Location location) {
        return distanceMap.get(location);
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * The angle relative to the direction EAST.
     *
     * @param location never null
     * @return in Cartesian coordinates
     */
    public double getAngle(Location location) {
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        double latitudeDifference = location.latitude - latitude;
        double longitudeDifference = location.longitude - longitude;
        return Math.atan2(latitudeDifference, longitudeDifference);
    }
}
