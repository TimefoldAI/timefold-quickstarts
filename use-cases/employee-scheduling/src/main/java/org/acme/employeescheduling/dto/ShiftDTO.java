package org.acme.employeescheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A shift at a location that must be covered by an employee with a required skill.")
public record ShiftDTO(
        @Schema(description = "Unique identifier of the shift.") String id,
        @Schema(description = "Start date and time of the shift in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).") String start,
        @Schema(description = "End date and time of the shift in ISO-8601 format (yyyy-MM-ddTHH:mm:ss).") String end,
        @Schema(description = "Location where the shift takes place.") String location,
        @Schema(description = "Skill required to cover the shift.") String requiredSkill,
        @Schema(description = "ID of the employee assigned to the shift. Null when unassigned.") String employeeId) {

    @SuppressWarnings("PMD.NullAssignment")
    public ShiftDTO {
        id = id == null ? "" : id;
        location = location == null ? "" : location;
        requiredSkill = requiredSkill == null ? "" : requiredSkill;
        employeeId = employeeId != null && employeeId.isBlank() ? null : employeeId;
    }

    public ShiftDTO withId(String id) {
        return new ShiftDTO(id, start, end, location, requiredSkill, employeeId);
    }

    public ShiftDTO withStart(String start) {
        return new ShiftDTO(id, start, end, location, requiredSkill, employeeId);
    }

    public ShiftDTO withEnd(String end) {
        return new ShiftDTO(id, start, end, location, requiredSkill, employeeId);
    }

    public ShiftDTO withLocation(String location) {
        return new ShiftDTO(id, start, end, location, requiredSkill, employeeId);
    }

    public ShiftDTO withRequiredSkill(String requiredSkill) {
        return new ShiftDTO(id, start, end, location, requiredSkill, employeeId);
    }

    public ShiftDTO withEmployeeId(String employeeId) {
        return new ShiftDTO(id, start, end, location, requiredSkill, employeeId);
    }
}
