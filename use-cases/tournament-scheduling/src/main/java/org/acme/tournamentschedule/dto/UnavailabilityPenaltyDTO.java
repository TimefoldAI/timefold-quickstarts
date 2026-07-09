package org.acme.tournamentschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A penalty expressing that a team is unavailable on a given day.")
public record UnavailabilityPenaltyDTO(
        @Schema(description = "ID of the unavailable team.") String teamId,
        @Schema(description = "Index of the day on which the team is unavailable.") int dateIndex) {

    public UnavailabilityPenaltyDTO {
        if (dateIndex < 0) {
            throw new IllegalArgumentException("Day index must not be negative.");
        }
    }

    public UnavailabilityPenaltyDTO withTeamId(String teamId) {
        return new UnavailabilityPenaltyDTO(teamId, dateIndex);
    }

    public UnavailabilityPenaltyDTO withDateIndex(int dateIndex) {
        return new UnavailabilityPenaltyDTO(teamId, dateIndex);
    }
}
