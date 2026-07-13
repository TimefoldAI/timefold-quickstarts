
package org.acme.conferencescheduling.domain;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.SequencedSet;

public class Speaker {

    private String id;
    private String name;

    private SequencedSet<Timeslot> unavailableTimeslots;

    private SequencedSet<String> requiredTimeslotTags;
    private SequencedSet<String> preferredTimeslotTags;
    private SequencedSet<String> prohibitedTimeslotTags;
    private SequencedSet<String> undesiredTimeslotTags;
    private SequencedSet<String> requiredRoomTags;
    private SequencedSet<String> preferredRoomTags;
    private SequencedSet<String> prohibitedRoomTags;
    private SequencedSet<String> undesiredRoomTags;

    public Speaker() {
    }

    public Speaker(String id, String name) {
        this(builder(id, name));
    }

    public Speaker(String name) {
        this(builder(name, name));
    }

    private Speaker(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unavailableTimeslots = builder.unavailableTimeslots;
        this.requiredTimeslotTags = builder.requiredTimeslotTags;
        this.preferredTimeslotTags = builder.preferredTimeslotTags;
        this.prohibitedTimeslotTags = builder.prohibitedTimeslotTags;
        this.undesiredTimeslotTags = builder.undesiredTimeslotTags;
        this.requiredRoomTags = builder.requiredRoomTags;
        this.preferredRoomTags = builder.preferredRoomTags;
        this.prohibitedRoomTags = builder.prohibitedRoomTags;
        this.undesiredRoomTags = builder.undesiredRoomTags;
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    // In a fluent builder, methods named after the fields they set are the whole point.
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {

        private final String id;
        private final String name;
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

        // CPD-OFF: builder boilerplate intentionally mirrors the Talk builder.
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

        // CPD-ON

        public Speaker build() {
            return new Speaker(this);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SequencedSet<Timeslot> getUnavailableTimeslots() {
        return unavailableTimeslots;
    }

    public void setUnavailableTimeslots(SequencedSet<Timeslot> unavailableTimeslots) {
        this.unavailableTimeslots = unavailableTimeslots;
    }

    public SequencedSet<String> getRequiredTimeslotTags() {
        return requiredTimeslotTags;
    }

    public void setRequiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
        this.requiredTimeslotTags = requiredTimeslotTags;
    }

    public SequencedSet<String> getPreferredTimeslotTags() {
        return preferredTimeslotTags;
    }

    public void setPreferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
        this.preferredTimeslotTags = preferredTimeslotTags;
    }

    public SequencedSet<String> getProhibitedTimeslotTags() {
        return prohibitedTimeslotTags;
    }

    public void setProhibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
    }

    public SequencedSet<String> getUndesiredTimeslotTags() {
        return undesiredTimeslotTags;
    }

    public void setUndesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
        this.undesiredTimeslotTags = undesiredTimeslotTags;
    }

    public SequencedSet<String> getRequiredRoomTags() {
        return requiredRoomTags;
    }

    public void setRequiredRoomTags(SequencedSet<String> requiredRoomTags) {
        this.requiredRoomTags = requiredRoomTags;
    }

    public SequencedSet<String> getPreferredRoomTags() {
        return preferredRoomTags;
    }

    public void setPreferredRoomTags(SequencedSet<String> preferredRoomTags) {
        this.preferredRoomTags = preferredRoomTags;
    }

    public SequencedSet<String> getProhibitedRoomTags() {
        return prohibitedRoomTags;
    }

    public void setProhibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
        this.prohibitedRoomTags = prohibitedRoomTags;
    }

    public SequencedSet<String> getUndesiredRoomTags() {
        return undesiredRoomTags;
    }

    public void setUndesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
        this.undesiredRoomTags = undesiredRoomTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Speaker speaker)) {
            return false;
        }
        return Objects.equals(getId(), speaker.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
