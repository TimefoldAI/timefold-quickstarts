package org.acme.employeescheduling.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "The employee a shift is assigned to.")
public record ShiftOutputDTO(
        @Schema(description = "Unique identifier of the shift.", required = true, minLength = 1) String id,
        @Schema(description = "Name of the employee working the shift, or null if still unassigned.") String employeeName) {
}
