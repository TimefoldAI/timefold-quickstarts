package org.acme.conferencescheduling.dto;

import static java.util.Objects.requireNonNull;
import static org.acme.conferencescheduling.dto.DTOHelper.immutableCopy;

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

    public SpeakerDTO {
        unavailableTimeslotIds = immutableCopy(unavailableTimeslotIds);
        requiredTimeslotTags = immutableCopy(requiredTimeslotTags);
        preferredTimeslotTags = immutableCopy(preferredTimeslotTags);
        prohibitedTimeslotTags = immutableCopy(prohibitedTimeslotTags);
        undesiredTimeslotTags = immutableCopy(undesiredTimeslotTags);
        requiredRoomTags = immutableCopy(requiredRoomTags);
        preferredRoomTags = immutableCopy(preferredRoomTags);
        prohibitedRoomTags = immutableCopy(prohibitedRoomTags);
        undesiredRoomTags = immutableCopy(undesiredRoomTags);
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public static final class Builder {

        private final String id;
        private final String name;
        private List<String> unavailableTimeslotIds;
        private List<String> requiredTimeslotTags;
        private List<String> preferredTimeslotTags;
        private List<String> prohibitedTimeslotTags;
        private List<String> undesiredTimeslotTags;
        private List<String> requiredRoomTags;
        private List<String> preferredRoomTags;
        private List<String> prohibitedRoomTags;
        private List<String> undesiredRoomTags;

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder unavailableTimeslotIds(List<String> unavailableTimeslotIds) {
            this.unavailableTimeslotIds = unavailableTimeslotIds;
            return this;
        }

        public Builder requiredTimeslotTags(List<String> requiredTimeslotTags) {
            this.requiredTimeslotTags = requiredTimeslotTags;
            return this;
        }

        public Builder preferredTimeslotTags(List<String> preferredTimeslotTags) {
            this.preferredTimeslotTags = preferredTimeslotTags;
            return this;
        }

        public Builder prohibitedTimeslotTags(List<String> prohibitedTimeslotTags) {
            this.prohibitedTimeslotTags = prohibitedTimeslotTags;
            return this;
        }

        public Builder undesiredTimeslotTags(List<String> undesiredTimeslotTags) {
            this.undesiredTimeslotTags = undesiredTimeslotTags;
            return this;
        }

        public Builder requiredRoomTags(List<String> requiredRoomTags) {
            this.requiredRoomTags = requiredRoomTags;
            return this;
        }

        public Builder preferredRoomTags(List<String> preferredRoomTags) {
            this.preferredRoomTags = preferredRoomTags;
            return this;
        }

        public Builder prohibitedRoomTags(List<String> prohibitedRoomTags) {
            this.prohibitedRoomTags = prohibitedRoomTags;
            return this;
        }

        public Builder undesiredRoomTags(List<String> undesiredRoomTags) {
            this.undesiredRoomTags = undesiredRoomTags;
            return this;
        }

        public SpeakerDTO build() {
            return new SpeakerDTO(id, name, unavailableTimeslotIds, requiredTimeslotTags, preferredTimeslotTags,
                    prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags,
                    prohibitedRoomTags, undesiredRoomTags);
        }
    }
}
