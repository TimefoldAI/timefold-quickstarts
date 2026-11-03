package org.acme.vehiclerouting.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A geographic coordinate.")
public record LocationInputDTO(
        @Schema(description = "Latitude in degrees.", required = true, minimum = "-90",
                maximum = "90") Double latitude,
        @Schema(description = "Longitude in degrees.", required = true, minimum = "-180",
                maximum = "180") Double longitude) {
}
