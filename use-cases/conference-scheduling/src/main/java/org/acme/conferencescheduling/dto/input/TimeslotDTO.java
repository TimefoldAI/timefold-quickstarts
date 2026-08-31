package org.acme.conferencescheduling.dto.input;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A timeslot during which talks can be scheduled.")
public record TimeslotDTO(
        @Schema(description = "Unique identifier of the timeslot.", required = true) @NotBlank String id,
        @Schema(description = "Start date-time, in ISO-8601 format with a UTC offset.") @NotNull OffsetDateTime startDateTime,
        @Schema(description = "End date-time, in ISO-8601 format with a UTC offset.") @NotNull OffsetDateTime endDateTime,
        @Schema(description = "Names of the talk types compatible with this timeslot.") @NotEmpty List<String> talkTypeNames,
        @Schema(description = "Tags describing this timeslot.") List<String> tags) {

    public TimeslotDTO {
        tags = tags != null ? tags : emptyList();
    }
}
