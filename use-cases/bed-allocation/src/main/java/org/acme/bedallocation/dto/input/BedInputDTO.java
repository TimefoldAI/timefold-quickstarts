package org.acme.bedallocation.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single bed inside a room.")
public record BedInputDTO(
        @Schema(description = "Unique identifier of the bed.", required = true, minLength = 1) String id) {
}
