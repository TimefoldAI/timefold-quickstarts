package org.acme.tournamentschedule.dto.input;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A day a team is unavailable to play a match.")
public record UnavailabilityInputDTO(
        @Schema(description = "ID of the unavailable team.", required = true, minLength = 1) String teamId,
        @Schema(description = "The day the team is unavailable on, in ISO-8601 date format.",
                required = true) LocalDate date) {
}
