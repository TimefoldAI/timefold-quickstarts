package org.acme.meetingschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A 15-minute time granule that a meeting can start on.")
public record TimeGrainDTO(
        @Schema(description = "Unique identifier of the time grain.") String id,
        @Schema(description = "Sequential index of the time grain across the planning horizon.") int grainIndex,
        @Schema(description = "Day of the year on which the time grain occurs.") int dayOfYear,
        @Schema(description = "Minute of the day at which the time grain starts.") int startingMinuteOfDay) {

    public TimeGrainDTO {
        // no-op compact constructor required by repository rules
    }

    public TimeGrainDTO withId(String id) {
        return new TimeGrainDTO(id, grainIndex, dayOfYear, startingMinuteOfDay);
    }

    public TimeGrainDTO withGrainIndex(int grainIndex) {
        return new TimeGrainDTO(id, grainIndex, dayOfYear, startingMinuteOfDay);
    }

    public TimeGrainDTO withDayOfYear(int dayOfYear) {
        return new TimeGrainDTO(id, grainIndex, dayOfYear, startingMinuteOfDay);
    }

    public TimeGrainDTO withStartingMinuteOfDay(int startingMinuteOfDay) {
        return new TimeGrainDTO(id, grainIndex, dayOfYear, startingMinuteOfDay);
    }
}
