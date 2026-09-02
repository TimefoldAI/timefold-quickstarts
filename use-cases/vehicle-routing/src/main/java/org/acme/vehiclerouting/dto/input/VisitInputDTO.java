package org.acme.vehiclerouting.dto.input;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A visit that a vehicle has to service, within the time window the visit accepts it in.")
public record VisitInputDTO(
        @Schema(description = "Unique identifier of the visit.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the visit, for example the name of the customer.", required = true,
                minLength = 1) String name,
        @Schema(description = "The location to drive to.", required = true) LocationInputDTO location,
        @Schema(description = "How much of a vehicle's capacity this visit takes up.", required = true,
                minimum = "0") Integer demand,
        @Schema(description = "Earliest time servicing may start, in ISO-8601 date-time format with an offset. "
                + "A vehicle that arrives earlier waits.", required = true) OffsetDateTime minStartTime,
        @Schema(description = "Latest time servicing must be finished by, in ISO-8601 date-time format with an "
                + "offset.", required = true) OffsetDateTime maxEndTime,
        @Schema(description = "How many minutes servicing this visit takes, on top of driving to it.", required = true,
                minimum = "0") Integer serviceDurationMinutes) {
}
