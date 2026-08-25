package org.acme.bedallocation.dto;

import jakarta.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single bed inside a room.")
public record BedDTO(
        @Schema(description = "Unique identifier of the bed.") @NotBlank String id) {
}
