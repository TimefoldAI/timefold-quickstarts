package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a flight number validation issue.")
public record FlightIdDetail(
        @Schema(description = "The flight number of the flight.") String flightNumber) implements IssueMetadata {

    public FlightIdDetail {
        flightNumber = flightNumber == null ? "" : flightNumber;
    }

    public FlightIdDetail withFlightNumber(String flightNumber) {
        return new FlightIdDetail(flightNumber);
    }

    @Override
    public String getType() {
        return "FlightNumber";
    }
}
