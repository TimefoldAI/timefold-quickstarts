package org.acme.sportsleagueschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A match between a home team and an away team, scheduled on a round.")
public record MatchDTO(
        @Schema(description = "Unique identifier of the match.") String id,
        @Schema(description = "ID of the home team.") String homeTeamId,
        @Schema(description = "ID of the away team.") String awayTeamId,
        @Schema(description = "Whether the match is a classic/important match, e.g. a derby.") boolean classicMatch,
        @Schema(description = "Index of the round the match is scheduled on. Null when unassigned.") Integer roundIndex) {

    public MatchDTO {
        id = id == null ? "" : id;
    }

    public MatchDTO withId(String id) {
        return new MatchDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
    }

    public MatchDTO withHomeTeamId(String homeTeamId) {
        return new MatchDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
    }

    public MatchDTO withAwayTeamId(String awayTeamId) {
        return new MatchDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
    }

    public MatchDTO withClassicMatch(boolean classicMatch) {
        return new MatchDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
    }

    public MatchDTO withRoundIndex(Integer roundIndex) {
        return new MatchDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
    }
}
