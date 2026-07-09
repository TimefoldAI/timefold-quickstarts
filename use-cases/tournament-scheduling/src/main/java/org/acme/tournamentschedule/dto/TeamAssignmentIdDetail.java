package org.acme.tournamentschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a team assignment ID validation issue.")
public record TeamAssignmentIdDetail(
        @Schema(description = "The ID of the team assignment.") String teamAssignmentId) implements IssueMetadata {

    public TeamAssignmentIdDetail {
        teamAssignmentId = teamAssignmentId == null ? "" : teamAssignmentId;
    }

    public TeamAssignmentIdDetail withTeamAssignmentId(String teamAssignmentId) {
        return new TeamAssignmentIdDetail(teamAssignmentId);
    }

    @Override
    public String getType() {
        return "TeamAssignmentId";
    }
}
