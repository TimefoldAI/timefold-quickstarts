package org.acme.bedallocation.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A patient stay that is either assigned to a bed, or not.")
public record StayOutputDTO(
        @Schema(description = "Unique identifier of the stay.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the bed this stay is assigned to, or null if unassigned.") String bedId) {
}
