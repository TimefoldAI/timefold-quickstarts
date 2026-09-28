package org.acme.facilitylocation.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A consumer that is either served by a facility, or not.")
public record ConsumerOutputDTO(
        @Schema(description = "Unique identifier of the consumer.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the facility serving this consumer, or null if it is unassigned.") String facilityId,
        @Schema(description = "Distance in meters between this consumer and its facility, or null if it is unassigned.") Long distanceInMeters) {
}
