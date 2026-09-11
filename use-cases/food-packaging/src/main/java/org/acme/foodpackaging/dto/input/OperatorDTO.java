package org.acme.foodpackaging.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "An operator who can run production lines and do their changeover cleaning.")
public record OperatorDTO(
        @Schema(description = "Unique identifier of the operator.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the operator.", required = true, minLength = 1) String name) {
}
