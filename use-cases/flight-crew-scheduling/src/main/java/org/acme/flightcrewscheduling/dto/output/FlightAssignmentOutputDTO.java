package org.acme.flightcrewscheduling.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A crew seat that is either assigned to a crew member, or not.")
public record FlightAssignmentOutputDTO(
        @Schema(description = "Unique identifier of the flight assignment.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the crew member assigned to this seat, or null if unassigned.") String employeeId) {
}
