package org.acme.vehiclerouting.dto.output;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A vehicle with the route it drives.")
public record VehicleOutputDTO(
        @Schema(description = "Unique identifier of the vehicle.", required = true, minLength = 1) String id,
        @Schema(description = "IDs of the visits on this vehicle's route, in the order it services them. "
                + "Empty when the vehicle stays home.", required = true) List<String> visitIds,
        @Schema(description = "The demand of every visit on this route added up.") Integer totalDemand,
        @Schema(description = "The driving time of the whole route in seconds, home location to home "
                + "location.") Long totalDrivingTimeSeconds,
        @Schema(description = "The time the vehicle is back at its home location, or null while its route is not "
                + "timed yet.") OffsetDateTime arrivalTime) {
}
