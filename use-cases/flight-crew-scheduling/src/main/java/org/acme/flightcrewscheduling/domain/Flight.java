package org.acme.flightcrewscheduling.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Flight(
        @PlanningId String flightNumber,
        Airport departureAirport,
        OffsetDateTime departureUTCDateTime,
        Airport arrivalAirport,
        OffsetDateTime arrivalUTCDateTime) {

    public LocalDate departureUTCDate() {
        return departureUTCDateTime.toLocalDate();
    }

    public LocalDate arrivalUTCDate() {
        return arrivalUTCDateTime.toLocalDate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Flight flight)) {
            return false;
        }
        return Objects.equals(flightNumber, flight.flightNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(flightNumber);
    }

    @Override
    public String toString() {
        return flightNumber;
    }
}
