package org.acme.flightcrewscheduling.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An airport a flight can depart from or arrive at.")
public record AirportInputDTO(
        @Schema(description = "Unique IATA 3-letter code of the airport.", required = true, minLength = 3,
                maxLength = 3) String code,
        @Schema(description = "Display name of the airport.", required = true, minLength = 1) String name,
        @Schema(description = "Latitude of the airport in decimal degrees.", required = true, minimum = "-90",
                maximum = "90") Double latitude,
        @Schema(description = "Longitude of the airport in decimal degrees.", required = true, minimum = "-180",
                maximum = "180") Double longitude) {
}
