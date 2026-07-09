package org.acme.tournamentschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a team ID validation issue.")
public record TeamIdDetail(
        @Schema(description = "The ID of the team.") String teamId) implements IssueMetadata {

    public TeamIdDetail {
        teamId = teamId == null ? "" : teamId;
    }

    public TeamIdDetail withTeamId(String teamId) {
        return new TeamIdDetail(teamId);
    }

    @Override
    public String getType() {
        return "TeamId";
    }
}
