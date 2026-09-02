package org.acme.flightcrewscheduling.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class FlightAssignment {

    @PlanningId
    private String id;
    private Flight flight;
    private int indexInFlight;
    private String requiredSkill;

    @PlanningVariable
    private Employee employee;

    public FlightAssignment() {
    }

    public FlightAssignment(String id, Flight flight, int indexInFlight, String requiredSkill, Employee employee) {
        this.id = id;
        this.flight = flight;
        this.indexInFlight = indexInFlight;
        this.requiredSkill = requiredSkill;
        this.employee = employee;
    }

    public boolean hasRequiredSkill() {
        return employee.hasSkill(requiredSkill);
    }

    public boolean isUnavailableEmployee() {
        return !employee.isAvailable(flight.departureUTCDate(), flight.arrivalUTCDate());
    }

    public OffsetDateTime getDepartureUTCDateTime() {
        return flight.departureUTCDateTime();
    }

    public OffsetDateTime getArrivalUTCDateTime() {
        return flight.arrivalUTCDateTime();
    }

    public Airport getDepartureAirport() {
        return flight.departureAirport();
    }

    public Airport getArrivalAirport() {
        return flight.arrivalAirport();
    }

    @Override
    public String toString() {
        return id;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public int getIndexInFlight() {
        return indexInFlight;
    }

    public void setIndexInFlight(int indexInFlight) {
        this.indexInFlight = indexInFlight;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlightAssignment that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
