package org.acme.facilitylocation.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A geographical position, in decimal degrees.")
public record LocationDTO(
        @Schema(description = "Latitude in decimal degrees.", required = true, minimum = "-90",
                maximum = "90") Double latitude,
        @Schema(description = "Longitude in decimal degrees.", required = true, minimum = "-180",
                maximum = "180") Double longitude) {
}
