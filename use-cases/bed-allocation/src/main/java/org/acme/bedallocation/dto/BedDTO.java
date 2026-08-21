package org.acme.bedallocation.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single bed inside a room.")
public record BedDTO(
        @Schema(description = "Unique identifier of the bed.", required = true) String id,
        @Schema(description = "Index of this bed within its room.", required = true) int indexInRoom) {
}
