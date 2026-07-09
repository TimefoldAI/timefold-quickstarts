package org.acme.tournamentschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A day (round) on which team assignments take place.")
public record DayDTO(
        @Schema(description = "Zero-based index of the day within the tournament.") int dateIndex) {

    public DayDTO {
        if (dateIndex < 0) {
            throw new IllegalArgumentException("Day index must not be negative.");
        }
    }

    public DayDTO withDateIndex(int dateIndex) {
        return new DayDTO(dateIndex);
    }
}
