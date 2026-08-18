
package org.acme.conferencescheduling.domain;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.SequencedSet;

public record Speaker(
        String id,
        String name,
        SequencedSet<Timeslot> unavailableTimeslots,
        SequencedSet<String> requiredTimeslotTags,
        SequencedSet<String> preferredTimeslotTags,
        SequencedSet<String> prohibitedTimeslotTags,
        SequencedSet<String> undesiredTimeslotTags,
        SequencedSet<String> requiredRoomTags,
        SequencedSet<String> preferredRoomTags,
        SequencedSet<String> prohibitedRoomTags,
        SequencedSet<String> undesiredRoomTags) {

    public static Builder builder(String id) {
        return new Builder(id, id);
    }

    public static final class Builder {

        private final String id;
        private String name;
        private SequencedSet<Timeslot> unavailableTimeslots = new LinkedHashSet<>();
        private SequencedSet<String> requiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> requiredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredRoomTags = new LinkedHashSet<>();

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder unavailableTimeslots(SequencedSet<Timeslot> unavailableTimeslots) {
            this.unavailableTimeslots = unavailableTimeslots;
            return this;
        }

        public Builder requiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
            this.requiredTimeslotTags = requiredTimeslotTags;
            return this;
        }

        public Builder preferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
            this.preferredTimeslotTags = preferredTimeslotTags;
            return this;
        }

        public Builder prohibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
            this.prohibitedTimeslotTags = prohibitedTimeslotTags;
            return this;
        }

        public Builder undesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
            this.undesiredTimeslotTags = undesiredTimeslotTags;
            return this;
        }

        public Builder requiredRoomTags(SequencedSet<String> requiredRoomTags) {
            this.requiredRoomTags = requiredRoomTags;
            return this;
        }

        public Builder preferredRoomTags(SequencedSet<String> preferredRoomTags) {
            this.preferredRoomTags = preferredRoomTags;
            return this;
        }

        public Builder prohibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
            this.prohibitedRoomTags = prohibitedRoomTags;
            return this;
        }

        public Builder undesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
            this.undesiredRoomTags = undesiredRoomTags;
            return this;
        }

        public Speaker build() {
            return new Speaker(id, name, unavailableTimeslots, requiredTimeslotTags, preferredTimeslotTags,
                    prohibitedTimeslotTags, undesiredTimeslotTags, requiredRoomTags, preferredRoomTags,
                    prohibitedRoomTags, undesiredRoomTags);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Speaker speaker)) {
            return false;
        }
        return Objects.equals(id, speaker.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
