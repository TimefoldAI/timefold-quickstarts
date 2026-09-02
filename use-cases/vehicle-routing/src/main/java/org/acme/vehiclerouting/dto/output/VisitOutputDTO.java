package org.acme.vehiclerouting.dto.output;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A visit that is either serviced by a vehicle at a given time, or not serviced at all.")
public record VisitOutputDTO(
        @Schema(description = "Unique identifier of the visit.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the vehicle servicing this visit, or null if unassigned.") String vehicleId,
        @Schema(description = "The time the vehicle arrives, which may be before the visit is ready, "
                + "or null if unassigned.") OffsetDateTime arrivalTime,
        @Schema(description = "The time servicing starts: the arrival time, or the visit's earliest start time when "
                + "the vehicle has to wait. Null if unassigned.") OffsetDateTime startServiceTime,
        @Schema(description = "The time the vehicle leaves again, or null if unassigned.") OffsetDateTime departureTime,
        @Schema(description = "Driving time in seconds from the previous stop on the route (the vehicle's home "
                + "location for the first visit), or null if unassigned.") Long drivingTimeSecondsFromPreviousStandstill) {
}
