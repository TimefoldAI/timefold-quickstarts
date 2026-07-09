package org.acme.tournamentschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A team that competes in the tournament.")
public record TeamDTO(
        @Schema(description = "Unique identifier of the team.") String id,
        @Schema(description = "Display name of the team.") String name) {

    public TeamDTO {
        name = name == null ? "" : name;
    }

    public TeamDTO withId(String id) {
        return new TeamDTO(id, name);
    }

    public TeamDTO withName(String name) {
        return new TeamDTO(id, name);
    }
}
