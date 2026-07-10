
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
        this(id, name, new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(),
                new LinkedHashSet<>());
    }

    public Speaker(String name) {
        this(name, name, new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(),
                new LinkedHashSet<>());
    }

    public Speaker(String id, String name, SequencedSet<String> undesiredTimeslotTags) {
        this(id, name, new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), undesiredTimeslotTags, new LinkedHashSet<>(), new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new LinkedHashSet<>());
    }

    public Speaker(String id, String name, SequencedSet<Timeslot> unavailableTimeslots, SequencedSet<String> requiredTimeslotTags,
            SequencedSet<String> preferredTimeslotTags, SequencedSet<String> prohibitedTimeslotTags, SequencedSet<String> undesiredTimeslotTags,
            SequencedSet<String> requiredRoomTags, SequencedSet<String> preferredRoomTags, SequencedSet<String> prohibitedRoomTags,
            SequencedSet<String> undesiredRoomTags) {
        this.id = id;
        this.name = name;
        this.unavailableTimeslots = unavailableTimeslots;
        this.requiredTimeslotTags = requiredTimeslotTags;
        this.preferredTimeslotTags = preferredTimeslotTags;
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
        this.undesiredTimeslotTags = undesiredTimeslotTags;
        this.requiredRoomTags = requiredRoomTags;
        this.preferredRoomTags = preferredRoomTags;
        this.prohibitedRoomTags = prohibitedRoomTags;
        this.undesiredRoomTags = undesiredRoomTags;
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
        if (this == o) return true;
        if (!(o instanceof Speaker speaker)) return false;
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
