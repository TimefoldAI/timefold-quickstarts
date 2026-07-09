package org.acme.sportsleagueschedule.dto;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A team that plays home and away matches against other teams.")
public record TeamDTO(
        @Schema(description = "Unique identifier of the team.") String id,
        @Schema(description = "Display name of the team.") String name,
        @Schema(description = "Distance in kilometers from this team to every other team, keyed by team ID.") Map<String, Integer> distanceToTeamId) {

    public TeamDTO {
        name = name == null ? "" : name;
        distanceToTeamId = distanceToTeamId == null ? Map.of() : Map.copyOf(distanceToTeamId);
    }

    public TeamDTO withId(String id) {
        return new TeamDTO(id, name, distanceToTeamId);
    }

    public TeamDTO withName(String name) {
        return new TeamDTO(id, name, distanceToTeamId);
    }

    public TeamDTO withDistanceToTeamId(Map<String, Integer> distanceToTeamId) {
        return new TeamDTO(id, name, distanceToTeamId);
    }
}
