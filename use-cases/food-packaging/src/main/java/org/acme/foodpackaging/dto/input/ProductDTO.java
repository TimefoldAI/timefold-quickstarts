package org.acme.foodpackaging.dto.input;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A product that is packaged by the jobs of this schedule.")
public record ProductDTO(
        @Schema(description = "Unique identifier of the product.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the product.", required = true, minLength = 1) String name,
        @Schema(description = "Cleaning duration for every product that can be produced right before this one on the "
                + "same line. A line only ever switches between products it has a cleaning duration for, so every "
                + "other product of the dataset must be listed here.",
                required = true) List<CleaningDurationDTO> cleaningDurations) {
}
