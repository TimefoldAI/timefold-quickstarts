package org.acme.facilitylocation.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A facility with the load the solution puts on it.")
public record FacilityOutputDTO(
        @Schema(description = "Unique identifier of the facility.", required = true, minLength = 1) String id,
        @Schema(description = "Whether this facility serves at least one consumer, so its setup cost has to be paid.",
                required = true) boolean used,
        @Schema(description = "Cumulative demand of the consumers served by this facility.", required = true,
                minimum = "0") long usedCapacity,
        @Schema(description = "Number of consumers served by this facility.", required = true,
                minimum = "0") int consumerCount) {
}
