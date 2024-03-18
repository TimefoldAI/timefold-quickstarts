
package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

public class Speaker {

    private String id;
    private String name;

    private Set<Timeslot> unavailableTimeslots;

    private Set<String> requiredTimeslotTags;
    private Set<String> preferredTimeslotTags;
    private Set<String> prohibitedTimeslotTags;
    private Set<String> undesiredTimeslotTags;
    private Set<String> requiredRoomTags;
    private Set<String> preferredRoomTags;
    private Set<String> prohibitedRoomTags;
    private Set<String> undesiredRoomTags;

    public Speaker() {
    }

    public Speaker(String id, String name) {
        this(id, name, emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(),
                emptySet());
    }

    public Speaker(String name) {
        this(name, name, emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(),
                emptySet());
    }

    public Speaker(String id, String name, Set<String> undesiredTimeslotTags) {
        this(id, name, emptySet(), emptySet(), emptySet(), emptySet(), undesiredTimeslotTags, emptySet(), emptySet(),
                emptySet(),
                emptySet());
    }

    public Speaker(String id, String name, Set<Timeslot> unavailableTimeslots, Set<String> requiredTimeslotTags,
            Set<String> preferredTimeslotTags, Set<String> prohibitedTimeslotTags, Set<String> undesiredTimeslotTags,
            Set<String> requiredRoomTags, Set<String> preferredRoomTags, Set<String> prohibitedRoomTags,
            Set<String> undesiredRoomTags) {
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

    public Set<Timeslot> getUnavailableTimeslots() {
        return unavailableTimeslots;
    }

    public void setUnavailableTimeslots(Set<Timeslot> unavailableTimeslots) {
        this.unavailableTimeslots = unavailableTimeslots;
    }

    public Set<String> getRequiredTimeslotTags() {
        return requiredTimeslotTags;
    }

    public void setRequiredTimeslotTags(Set<String> requiredTimeslotTags) {
        this.requiredTimeslotTags = requiredTimeslotTags;
    }

    public Set<String> getPreferredTimeslotTags() {
        return preferredTimeslotTags;
    }

    public void setPreferredTimeslotTags(Set<String> preferredTimeslotTags) {
        this.preferredTimeslotTags = preferredTimeslotTags;
    }

    public Set<String> getProhibitedTimeslotTags() {
        return prohibitedTimeslotTags;
    }

    public void setProhibitedTimeslotTags(Set<String> prohibitedTimeslotTags) {
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
    }

    public Set<String> getUndesiredTimeslotTags() {
        return undesiredTimeslotTags;
    }

    public void setUndesiredTimeslotTags(Set<String> undesiredTimeslotTags) {
        this.undesiredTimeslotTags = undesiredTimeslotTags;
    }

    public Set<String> getRequiredRoomTags() {
        return requiredRoomTags;
    }

    public void setRequiredRoomTags(Set<String> requiredRoomTags) {
        this.requiredRoomTags = requiredRoomTags;
    }

    public Set<String> getPreferredRoomTags() {
        return preferredRoomTags;
    }

    public void setPreferredRoomTags(Set<String> preferredRoomTags) {
        this.preferredRoomTags = preferredRoomTags;
    }

    public Set<String> getProhibitedRoomTags() {
        return prohibitedRoomTags;
    }

    public void setProhibitedRoomTags(Set<String> prohibitedRoomTags) {
        this.prohibitedRoomTags = prohibitedRoomTags;
    }

    public Set<String> getUndesiredRoomTags() {
        return undesiredRoomTags;
    }

    public void setUndesiredRoomTags(Set<String> undesiredRoomTags) {
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
