package org.acme.foodpackaging.dto.output;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A production line with the operator running it and the jobs it produces, in production order.")
public record LineAssignmentDTO(
        @Schema(description = "Unique identifier of the line.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the operator running this line, or null if unassigned.") String operatorId,
        @Schema(description = "IDs of the jobs produced on this line, in production order.",
                required = true) List<String> jobIds) {
}
