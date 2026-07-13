package org.acme.conferencescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A timeslot during which talks can be scheduled.")
public record TimeslotDTO(
        @Schema(description = "Unique identifier of the timeslot.") String id,
        @Schema(description = "Local start date-time in ISO-8601 format.") String startDateTime,
        @Schema(description = "Local end date-time in ISO-8601 format.") String endDateTime,
        @Schema(description = "Names of the talk types compatible with this timeslot.") List<String> talkTypeNames,
        @Schema(description = "Tags describing this timeslot.") List<String> tags) {

    public TimeslotDTO {
        talkTypeNames = immutableCopy(talkTypeNames);
        tags = immutableCopy(tags);
    }

    private static List<String> immutableCopy(List<String> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
