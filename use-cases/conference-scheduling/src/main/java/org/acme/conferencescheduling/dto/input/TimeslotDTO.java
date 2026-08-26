package org.acme.conferencescheduling.dto.input;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Schema(description = "A timeslot during which talks can be scheduled.")
public record TimeslotDTO(
        @Schema(description = "Unique identifier of the timeslot.", required = true) @NotBlank String id,
        @Schema(description = "Local start date-time in ISO-8601 format.") @NotNull LocalDateTime startDateTime,
        @Schema(description = "Local end date-time in ISO-8601 format.") @NotNull LocalDateTime endDateTime,
        @Schema(description = "Names of the talk types compatible with this timeslot.") @NotEmpty List<String> talkTypeNames,
        @Schema(description = "Tags describing this timeslot.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> tags) {
}
