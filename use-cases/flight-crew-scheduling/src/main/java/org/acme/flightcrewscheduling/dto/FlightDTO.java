package org.acme.flightcrewscheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A scheduled flight between two airports.")
public record FlightDTO(
        @Schema(description = "Unique flight number identifying the flight.") String flightNumber,
        @Schema(description = "ID of the departure airport.") String departureAirportId,
        @Schema(description = "Departure date and time in UTC, in ISO-8601 format.") String departureUTCDateTime,
        @Schema(description = "ID of the arrival airport.") String arrivalAirportId,
        @Schema(description = "Arrival date and time in UTC, in ISO-8601 format.") String arrivalUTCDateTime) {

    public FlightDTO {
        // no-op compact constructor required by repository rules
    }

    public FlightDTO withFlightNumber(String flightNumber) {
        return new FlightDTO(flightNumber, departureAirportId, departureUTCDateTime, arrivalAirportId, arrivalUTCDateTime);
    }

    public FlightDTO withDepartureAirportId(String departureAirportId) {
        return new FlightDTO(flightNumber, departureAirportId, departureUTCDateTime, arrivalAirportId, arrivalUTCDateTime);
    }

    public FlightDTO withDepartureUTCDateTime(String departureUTCDateTime) {
        return new FlightDTO(flightNumber, departureAirportId, departureUTCDateTime, arrivalAirportId, arrivalUTCDateTime);
    }

    public FlightDTO withArrivalAirportId(String arrivalAirportId) {
        return new FlightDTO(flightNumber, departureAirportId, departureUTCDateTime, arrivalAirportId, arrivalUTCDateTime);
    }

    public FlightDTO withArrivalUTCDateTime(String arrivalUTCDateTime) {
        return new FlightDTO(flightNumber, departureAirportId, departureUTCDateTime, arrivalAirportId, arrivalUTCDateTime);
    }
}
