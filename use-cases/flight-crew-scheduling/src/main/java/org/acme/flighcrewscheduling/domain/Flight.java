package org.acme.flighcrewscheduling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Flight.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "flightNumber")
public class Flight implements Comparable<Flight> {

    private static final Comparator<Flight> COMPARATOR = Comparator.comparing(Flight::getDepartureUTCDateTime)
            .thenComparing(Flight::getDepartureAirport)
            .thenComparing(Flight::getArrivalUTCDateTime)
            .thenComparing(Flight::getArrivalAirport)
            .thenComparing(Flight::getFlightNumber);

    @PlanningId
    private String flightNumber;
    private Airport departureAirport;
    private LocalDateTime departureUTCDateTime;
    private Airport arrivalAirport;
    private LocalDateTime arrivalUTCDateTime;

    public Flight() {
    }

    public Flight(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Flight(String flightNumber, Airport departureAirport, Airport arrivalAirport) {
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
    }

    public Flight(String flightNumber, Airport departureAirport, LocalDateTime departureUTCDateTime,
            Airport arrivalAirport, LocalDateTime arrivalUTCDateTime) {
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.departureUTCDateTime = departureUTCDateTime;
        this.arrivalAirport = arrivalAirport;
        this.arrivalUTCDateTime = arrivalUTCDateTime;
    }

    @JsonIgnore
    public LocalDate getDepartureUTCDate() {
        return departureUTCDateTime.toLocalDate();
    }

    @Override
    public String toString() {
        return flightNumber + "@" + departureUTCDateTime.toLocalDate();
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(Airport departureAirport) {
        this.departureAirport = departureAirport;
    }

    public LocalDateTime getDepartureUTCDateTime() {
        return departureUTCDateTime;
    }

    public void setDepartureUTCDateTime(LocalDateTime departureUTCDateTime) {
        this.departureUTCDateTime = departureUTCDateTime;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(Airport arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public LocalDateTime getArrivalUTCDateTime() {
        return arrivalUTCDateTime;
    }

    public void setArrivalUTCDateTime(LocalDateTime arrivalUTCDateTime) {
        this.arrivalUTCDateTime = arrivalUTCDateTime;
    }

    @Override
    public int compareTo(Flight o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Flight flight))
            return false;
        return Objects.equals(getFlightNumber(), flight.getFlightNumber());
    }

    @Override
    public int hashCode() {
        return getFlightNumber().hashCode();
    }
}
