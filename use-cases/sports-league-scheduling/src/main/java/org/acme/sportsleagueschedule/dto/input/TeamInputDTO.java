package org.acme.sportsleagueschedule.dto.input;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A team in the league, with the travel distance from its own venue to every other venue.")
public record TeamInputDTO(
        @Schema(description = "Unique identifier of the team.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the team.", required = true, minLength = 1) String name,
        @Schema(description = "Distance in kilometres from this team's venue to every other team's venue, "
                + "keyed by that team's ID.", required = true) Map<String, Integer> distanceToTeam) {
}
