package org.acme.sportsleagueschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a match ID validation issue.")
public record MatchIdDetail(
        @Schema(description = "The ID of the match.") String matchId) implements IssueMetadata {

    public MatchIdDetail {
        matchId = matchId == null ? "" : matchId;
    }

    public MatchIdDetail withMatchId(String matchId) {
        return new MatchIdDetail(matchId);
    }

    @Override
    public String getType() {
        return "MatchId";
    }
}
