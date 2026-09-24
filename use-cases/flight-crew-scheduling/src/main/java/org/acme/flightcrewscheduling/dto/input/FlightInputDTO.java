package org.acme.flightcrewscheduling.dto.input;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A flight that needs a crew.")
public record FlightInputDTO(
        @Schema(description = "Unique flight number.", required = true, minLength = 1) String flightNumber,
        @Schema(description = "IATA code of the airport this flight departs from.", required = true, minLength = 3,
                maxLength = 3) String departureAirportCode,
        @Schema(description = "Departure date-time, in ISO-8601 format with a UTC offset.",
                required = true) OffsetDateTime departureUTCDateTime,
        @Schema(description = "IATA code of the airport this flight arrives at.", required = true, minLength = 3,
                maxLength = 3) String arrivalAirportCode,
        @Schema(description = "Arrival date-time, in ISO-8601 format with a UTC offset.",
                required = true) OffsetDateTime arrivalUTCDateTime) {
}
