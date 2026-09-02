package org.acme.flightcrewscheduling.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single crew seat on a flight that must be assigned to a crew member.")
public record FlightAssignmentInputDTO(
        @Schema(description = "Unique identifier of the flight assignment.", required = true, minLength = 1) String id,
        @Schema(description = "Number of the flight this seat belongs to.", required = true,
                minLength = 1) String flightNumber,
        @Schema(description = "Position of this seat within its flight's crew.", required = true,
                minimum = "1") Integer indexInFlight,
        @Schema(description = "Skill the crew member filling this seat must hold.", required = true,
                minLength = 1) String requiredSkill,
        @Schema(description = "ID of the crew member assigned to this seat, or null if unassigned.",
                minLength = 1) String employeeId) {

    public FlightAssignmentInputDTO withEmployeeId(String employeeId) {
        return new FlightAssignmentInputDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
    }
}
