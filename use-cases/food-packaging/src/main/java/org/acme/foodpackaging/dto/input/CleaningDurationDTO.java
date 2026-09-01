package org.acme.foodpackaging.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "How long a line has to be cleaned when it switches from one product to another.")
public record CleaningDurationDTO(
        @Schema(description = "ID of the product produced before the product this cleaning duration belongs to.",
                required = true, minLength = 1) String previousProductId,
        @Schema(description = "Duration of the cleaning in minutes.", required = true,
                minimum = "0") Long durationMinutes) {
}
