package org.acme.flightcrewscheduling.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public class Airport implements Comparable<Airport> {

    @PlanningId
    private String code; // IATA 3-letter code
    private String name;

    public Airport() {
    }

    public Airport(String code) {
        this.code = code;
    }

    public Airport(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

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

    @Override
    public int compareTo(Airport o) {
        return code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Airport airport)) {
            return false;
        }
        return Objects.equals(getCode(), airport.getCode());
    }

    @Override
    public int hashCode() {
        return getCode().hashCode();
    }
}
