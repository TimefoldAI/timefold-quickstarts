package org.acme.conferencescheduling.dto.input;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Schema(description = "A room in which talks can be scheduled.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.") @NotBlank String id,
        @Schema(description = "Display name of the room.") @NotBlank String name,
        @Schema(description = "Seating capacity of the room.") @NotNull @Min(1) Integer capacity,
        @Schema(description = "Names of the talk types compatible with this room. Null or empty means it's compatible with all talk types.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> talkTypeNames,
        @Schema(description = "IDs of the timeslots during which this room is unavailable.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> unavailableTimeslotIds,
        @Schema(description = "Tags describing this room.") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> tags) {
}
