package org.acme.facilitylocation.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A consumer whose demand has to be served by exactly one facility.")
public record ConsumerInputDTO(
        @Schema(description = "Unique identifier of the consumer.", required = true, minLength = 1) String id,
        @Schema(description = "Geographical position of the consumer.", required = true) LocationDTO location,
        @Schema(description = "Demand of this consumer, counted against the capacity of its facility.", required = true,
                minimum = "0") Long demand,
        @Schema(description = "ID of the facility serving this consumer, or null if it is not assigned yet.",
                minLength = 1) String facilityId) {

    public ConsumerInputDTO withFacilityId(String facilityId) {
        return new ConsumerInputDTO(id, location, demand, facilityId);
    }
}
