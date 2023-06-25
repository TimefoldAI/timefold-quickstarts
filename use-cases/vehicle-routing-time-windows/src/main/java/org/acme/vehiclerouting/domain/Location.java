package org.acme.vehiclerouting.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class Location {

    private double latitude;
    private double longitude;

    @JsonIgnore
    private Map<Location, Long> distanceMap;

    @JsonCreator
    public Location(@JsonProperty("latitude") double latitude, @JsonProperty("longitude") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<Location, Long> getDistanceMap() {
        return distanceMap;
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
}
