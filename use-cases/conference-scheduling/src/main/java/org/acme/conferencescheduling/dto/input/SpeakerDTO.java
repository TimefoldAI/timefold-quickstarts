package org.acme.conferencescheduling.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A speaker who presents one or more talks.")
public record SpeakerDTO(
        @Schema(description = "Unique identifier of the speaker.") @NotBlank String id,
        @Schema(description = "Display name of the speaker.") @NotBlank String name,
        @Schema(description = "IDs of the timeslots during which this speaker is unavailable.") List<String> unavailableTimeslotIds,
        @Schema(description = "Timeslot tag preferences of this speaker.") @Valid TagPreferencesDTO timeslotTags,
        @Schema(description = "Room tag preferences of this speaker.") @Valid TagPreferencesDTO roomTags) {

    public SpeakerDTO {
        unavailableTimeslotIds = unavailableTimeslotIds != null ? unavailableTimeslotIds : emptyList();
        timeslotTags = timeslotTags != null ? timeslotTags : TagPreferencesDTO.EMPTY;
        roomTags = roomTags != null ? roomTags : TagPreferencesDTO.EMPTY;
    }
}
