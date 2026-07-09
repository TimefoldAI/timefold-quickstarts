package org.acme.sportsleagueschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A round on which matches can be scheduled.")
public record RoundDTO(
        @Schema(description = "Zero-based index of the round.") int index,
        @Schema(description = "Whether the round falls on a weekend or holiday.") boolean weekendOrHoliday) {

    public RoundDTO {
        if (index < 0) {
            throw new IllegalArgumentException("Round index (" + index + ") must not be negative.");
        }
    }

    public RoundDTO withIndex(int index) {
        return new RoundDTO(index, weekendOrHoliday);
    }

    public RoundDTO withWeekendOrHoliday(boolean weekendOrHoliday) {
        return new RoundDTO(index, weekendOrHoliday);
    }
}
