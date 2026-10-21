package org.acme.employeescheduling.dto.input;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A shift at one location that needs one employee with the required skill.")
public record ShiftInputDTO(
        @Schema(description = "Unique identifier of the shift.", required = true, minLength = 1) String id,
        @Schema(description = "Date and time the shift starts, e.g. 2026-10-26T06:00:00Z.",
                required = true) OffsetDateTime start,
        @Schema(description = "Date and time the shift ends. Must be after the start.",
                required = true) OffsetDateTime end,
        @Schema(description = "Location the shift takes place at, e.g. Critical care.", required = true,
                minLength = 1) String location,
        @Schema(description = "Skill the employee working the shift must have, e.g. Nurse.", required = true,
                minLength = 1) String requiredSkill,
        @Schema(description = "Name of the employee working the shift, or null if not decided yet.",
                minLength = 1) String employeeName) {

    public ShiftInputDTO withEmployeeName(String employeeName) {
        return new ShiftInputDTO(id, start, end, location, requiredSkill, employeeName);
    }
}
