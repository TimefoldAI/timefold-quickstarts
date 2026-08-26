package org.acme.conferencescheduling.dto.input;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Schema(description = "A speaker who presents one or more talks.")
public record SpeakerDTO(
        @Schema(description = "Unique identifier of the speaker.") @NotBlank String id,
        @Schema(description = "Display name of the speaker.") String name,
        @Schema(description = "IDs of the timeslots during which this speaker is unavailable.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> unavailableTimeslotIds,
        @Schema(description = "Timeslot tags required by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> requiredTimeslotTags,
        @Schema(description = "Timeslot tags preferred by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> preferredTimeslotTags,
        @Schema(description = "Timeslot tags prohibited by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> prohibitedTimeslotTags,
        @Schema(description = "Timeslot tags undesired by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> undesiredTimeslotTags,
        @Schema(description = "Room tags required by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> requiredRoomTags,
        @Schema(description = "Room tags preferred by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> preferredRoomTags,
        @Schema(description = "Room tags prohibited by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> prohibitedRoomTags,
        @Schema(description = "Room tags undesired by this speaker.") @JsonSetter(
                nulls = Nulls.AS_EMPTY) List<String> undesiredRoomTags) {
}
