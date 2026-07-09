package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a flight assignment ID validation issue.")
public record FlightAssignmentIdDetail(
        @Schema(description = "The ID of the flight assignment.") String flightAssignmentId) implements IssueMetadata {

    public FlightAssignmentIdDetail {
        flightAssignmentId = flightAssignmentId == null ? "" : flightAssignmentId;
    }

    public FlightAssignmentIdDetail withFlightAssignmentId(String flightAssignmentId) {
        return new FlightAssignmentIdDetail(flightAssignmentId);
    }

    @Override
    public String getType() {
        return "FlightAssignmentId";
    }
}
