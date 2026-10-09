package org.acme.facilitylocation.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A candidate facility that can serve consumers, at the price of its setup cost.")
public record FacilityInputDTO(
        @Schema(description = "Unique identifier of the facility.", required = true, minLength = 1) String id,
        @Schema(description = "Geographical position of the facility.", required = true) LocationDTO location,
        @Schema(description = "Cost of opening this facility, paid once if it serves at least one consumer.",
                required = true, minimum = "0") Long setupCost,
        @Schema(description = "Total consumer demand this facility can serve.", required = true,
                minimum = "1") Long capacity) {
}
