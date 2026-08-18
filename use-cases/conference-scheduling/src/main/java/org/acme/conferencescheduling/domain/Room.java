package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public record Room(
        @PlanningId String id,
        String name,
        int capacity,
        Set<TalkType> talkTypes,
        Set<Timeslot> unavailableTimeslots,
        Set<String> tags) {

    public static Builder builder(String id) {
        return new Builder(id, id);
    }

    public static final class Builder {

        private final String id;
        private final String name;
        private int capacity = 0;
        private Set<TalkType> talkTypes = emptySet();
        private Set<Timeslot> unavailableTimeslots = emptySet();
        private Set<String> tags = emptySet();

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder talkTypes(Set<TalkType> talkTypes) {
            this.talkTypes = talkTypes;
            return this;
        }

        public Builder unavailableTimeslots(Set<Timeslot> unavailableTimeslots) {
            this.unavailableTimeslots = unavailableTimeslots;
            return this;
        }

        public Builder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Room build() {
            return new Room(id, name, capacity, talkTypes, unavailableTimeslots, tags);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room room)) {
            return false;
        }
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
}