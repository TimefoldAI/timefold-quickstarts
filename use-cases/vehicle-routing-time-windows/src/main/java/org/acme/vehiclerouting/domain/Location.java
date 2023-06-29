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
    private Map<Location, Long> drivingTimeSecondsMap;

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

    public Map<Location, Long> getDrivingTimeSecondsMap() {
        return drivingTimeSecondsMap;
    }

    /**
     * Set the driving time map (in seconds).
     *
     * @param drivingTimeSecondsMap a map containing driving time from here to other locations
     */
    public void setDrivingTimeSecondsMap(Map<Location, Long> drivingTimeSecondsMap) {
        this.drivingTimeSecondsMap = drivingTimeSecondsMap;
    }

    /**
     * Driving time to the given location in seconds.
     *
     * @param location other location
     * @return driving time in seconds
     */
    public long getDrivingTimeTo(Location location) {
        return drivingTimeSecondsMap.get(location);
    }
}
