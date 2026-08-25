package org.acme.bedallocation.dto.input;

import jakarta.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single bed inside a room.")
public record BedInputDTO(
        @Schema(description = "Unique identifier of the bed.") @NotBlank String id) {
}
