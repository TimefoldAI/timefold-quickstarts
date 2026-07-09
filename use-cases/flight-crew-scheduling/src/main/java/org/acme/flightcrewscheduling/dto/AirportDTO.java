package org.acme.flightcrewscheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An airport that flights depart from and arrive at.")
public record AirportDTO(
        @Schema(description = "Unique identifier of the airport, typically its IATA code.") String id,
        @Schema(description = "Display name of the airport.") String name) {

    public AirportDTO {
        name = name == null ? "" : name;
    }

    public AirportDTO withId(String id) {
        return new AirportDTO(id, name);
    }

    public AirportDTO withName(String name) {
        return new AirportDTO(id, name);
    }
}
