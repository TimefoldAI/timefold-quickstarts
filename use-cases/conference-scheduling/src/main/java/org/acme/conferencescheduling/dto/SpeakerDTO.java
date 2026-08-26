package org.acme.conferencescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A speaker who presents one or more talks.")
public record SpeakerDTO(
        @Schema(description = "Unique identifier of the speaker.", required = true) String id,
        @Schema(description = "Display name of the speaker.") String name,
        @Schema(description = "IDs of the timeslots during which this speaker is unavailable.") List<String> unavailableTimeslotIds,
        @Schema(description = "Timeslot tags required by this speaker.") List<String> requiredTimeslotTags,
        @Schema(description = "Timeslot tags preferred by this speaker.") List<String> preferredTimeslotTags,
        @Schema(description = "Timeslot tags prohibited by this speaker.") List<String> prohibitedTimeslotTags,
        @Schema(description = "Timeslot tags undesired by this speaker.") List<String> undesiredTimeslotTags,
        @Schema(description = "Room tags required by this speaker.") List<String> requiredRoomTags,
        @Schema(description = "Room tags preferred by this speaker.") List<String> preferredRoomTags,
        @Schema(description = "Room tags prohibited by this speaker.") List<String> prohibitedRoomTags,
        @Schema(description = "Room tags undesired by this speaker.") List<String> undesiredRoomTags) {
}
