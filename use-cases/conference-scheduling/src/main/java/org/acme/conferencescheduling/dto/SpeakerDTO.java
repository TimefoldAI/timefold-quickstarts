package org.acme.conferencescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A speaker who presents one or more talks.")
public record SpeakerDTO(
        @Schema(description = "Unique identifier of the speaker.") String id,
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

    public SpeakerDTO {
        name = name == null ? "" : name;
        unavailableTimeslotIds = unavailableTimeslotIds == null ? List.of() : List.copyOf(unavailableTimeslotIds);
        requiredTimeslotTags = requiredTimeslotTags == null ? List.of() : List.copyOf(requiredTimeslotTags);
        preferredTimeslotTags = preferredTimeslotTags == null ? List.of() : List.copyOf(preferredTimeslotTags);
        prohibitedTimeslotTags = prohibitedTimeslotTags == null ? List.of() : List.copyOf(prohibitedTimeslotTags);
        undesiredTimeslotTags = undesiredTimeslotTags == null ? List.of() : List.copyOf(undesiredTimeslotTags);
        requiredRoomTags = requiredRoomTags == null ? List.of() : List.copyOf(requiredRoomTags);
        preferredRoomTags = preferredRoomTags == null ? List.of() : List.copyOf(preferredRoomTags);
        prohibitedRoomTags = prohibitedRoomTags == null ? List.of() : List.copyOf(prohibitedRoomTags);
        undesiredRoomTags = undesiredRoomTags == null ? List.of() : List.copyOf(undesiredRoomTags);
    }

    public SpeakerDTO withId(String id) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withName(String name) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withUnavailableTimeslotIds(List<String> unavailableTimeslotIds) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withRequiredTimeslotTags(List<String> requiredTimeslotTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withPreferredTimeslotTags(List<String> preferredTimeslotTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withProhibitedTimeslotTags(List<String> prohibitedTimeslotTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withUndesiredTimeslotTags(List<String> undesiredTimeslotTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withRequiredRoomTags(List<String> requiredRoomTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withPreferredRoomTags(List<String> preferredRoomTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withProhibitedRoomTags(List<String> prohibitedRoomTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }

    public SpeakerDTO withUndesiredRoomTags(List<String> undesiredRoomTags) {
        return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags, prohibitedRoomTags,
                undesiredRoomTags);
    }
}
