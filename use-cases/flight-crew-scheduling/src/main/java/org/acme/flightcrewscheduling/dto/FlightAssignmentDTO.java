package org.acme.flightcrewscheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A crew slot on a flight that must be filled by an employee with the required skill.")
public record FlightAssignmentDTO(
        @Schema(description = "Unique identifier of the flight assignment.") String id,
        @Schema(description = "Flight number of the flight this assignment belongs to.") String flightNumber,
        @Schema(description = "Index of this crew slot within the flight.") int indexInFlight,
        @Schema(description = "Skill required to fill this crew slot.") String requiredSkill,
        @Schema(description = "ID of the employee assigned to this slot. Null when unassigned.") String employeeId) {

    public FlightAssignmentDTO {
        employeeId = normalizeId(employeeId);
        requiredSkill = requiredSkill == null ? "" : requiredSkill;
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public FlightAssignmentDTO withId(String id) {
        return new FlightAssignmentDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
    }

    public FlightAssignmentDTO withFlightNumber(String flightNumber) {
        return new FlightAssignmentDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
    }

    public FlightAssignmentDTO withIndexInFlight(int indexInFlight) {
        return new FlightAssignmentDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
    }

    public FlightAssignmentDTO withRequiredSkill(String requiredSkill) {
        return new FlightAssignmentDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
    }

    public FlightAssignmentDTO withEmployeeId(String employeeId) {
        return new FlightAssignmentDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
    }
}
