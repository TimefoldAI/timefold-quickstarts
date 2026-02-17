package org.acme.flighcrewscheduling.domain;

import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Airport.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "code")
public class Airport implements Comparable<Airport> {

    @PlanningId
    private String code; // IATA 3-letter code
    private String name;
    private double latitude;
    private double longitude;

    private Map<Airport, Long> taxiTimeInMinutes;

    public Airport() {
    }

    public Airport(String code) {
        this.code = code;
    }

    public Airport(String code, String name, double latitude, double longitude) {
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Map<Airport, Long> getTaxiTimeInMinutes() {
        return taxiTimeInMinutes;
    }

    public void setTaxiTimeInMinutes(Map<Airport, Long> taxiTimeInMinutes) {
        this.taxiTimeInMinutes = taxiTimeInMinutes;
    }

    @Override
    public int compareTo(Airport o) {
        return code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Airport airport))
            return false;
        return Objects.equals(getCode(), airport.getCode());
    }

    @Override
    public int hashCode() {
        return getCode().hashCode();
    }
}
