package org.acme.sportsleagueschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "One matchday of the league. Rounds are played back to back, in index order.")
public record RoundInputDTO(
        @Schema(description = "Position of the round in the season, starting at 0.", required = true,
                minimum = "0") Integer index,
        @Schema(description = "Whether the round falls on a weekend or a holiday, which is when classic matches "
                + "draw the biggest crowd.", required = true) Boolean weekendOrHoliday) {
}
