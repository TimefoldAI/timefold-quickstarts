package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about an airport ID validation issue.")
public record AirportIdDetail(
        @Schema(description = "The ID of the airport.") String airportId) implements IssueMetadata {

    public AirportIdDetail {
        airportId = airportId == null ? "" : airportId;
    }

    public AirportIdDetail withAirportId(String airportId) {
        return new AirportIdDetail(airportId);
    }

    @Override
    public String getType() {
        return "AirportId";
    }
}
