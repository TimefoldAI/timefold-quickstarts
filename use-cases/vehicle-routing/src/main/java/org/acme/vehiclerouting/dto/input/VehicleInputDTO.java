package org.acme.vehiclerouting.dto.input;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A vehicle that drives a route of visits, starting and ending at its home location.")
public record VehicleInputDTO(
        @Schema(description = "Unique identifier of the vehicle.", required = true, minLength = 1) String id,
        @Schema(description = "How much demand the vehicle can carry in total over its whole route.", required = true,
                minimum = "1") Integer capacity,
        @Schema(description = "The location the vehicle departs from and returns to.",
                required = true) LocationInputDTO homeLocation,
        @Schema(description = "The time the vehicle leaves its home location, in ISO-8601 date-time format with an "
                + "offset.", required = true) OffsetDateTime departureTime,
        @Schema(description = "IDs of the visits assigned to this vehicle, in the order it services them. "
                + "Empty when the vehicle has no route yet. A visit that appears in no vehicle's route is "
                + "unassigned.") List<String> visitIds) {

    public VehicleInputDTO {
        visitIds = visitIds != null ? visitIds : emptyList();
    }

    public VehicleInputDTO withVisitIds(List<String> visitIds) {
        return new VehicleInputDTO(id, capacity, homeLocation, departureTime, visitIds);
    }
}
