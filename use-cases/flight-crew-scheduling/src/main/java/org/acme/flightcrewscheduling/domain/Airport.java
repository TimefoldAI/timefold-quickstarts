package org.acme.flightcrewscheduling.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Airport(
        @PlanningId String code, // IATA 3-letter code
        String name,
        double latitude,
        double longitude) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Airport airport)) {
            return false;
        }
        return Objects.equals(code, airport.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
