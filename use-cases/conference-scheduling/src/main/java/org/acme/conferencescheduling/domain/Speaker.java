
package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptySet;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Speaker.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class Speaker {

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

    public Speaker(String name) {
        this(name, emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(),
                emptySet());
    }

    public Speaker(String name, Set<String> undesiredTimeslotTags) {
        this(name, emptySet(), emptySet(), emptySet(), emptySet(), undesiredTimeslotTags, emptySet(), emptySet(), emptySet(),
                emptySet());
    }

    public Speaker(String name, Set<Timeslot> unavailableTimeslots, Set<String> requiredTimeslotTags,
            Set<String> preferredTimeslotTags, Set<String> prohibitedTimeslotTags, Set<String> undesiredTimeslotTags,
            Set<String> requiredRoomTags, Set<String> preferredRoomTags, Set<String> prohibitedRoomTags,
            Set<String> undesiredRoomTags) {
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

    public String getName() {
        return name;
    }

    public Set<Timeslot> getUnavailableTimeslots() {
        return unavailableTimeslots;
    }

    public Set<String> getRequiredTimeslotTags() {
        return requiredTimeslotTags;
    }

    public Set<String> getPreferredTimeslotTags() {
        return preferredTimeslotTags;
    }

    public Set<String> getProhibitedTimeslotTags() {
        return prohibitedTimeslotTags;
    }

    public Set<String> getUndesiredTimeslotTags() {
        return undesiredTimeslotTags;
    }

    public Set<String> getRequiredRoomTags() {
        return requiredRoomTags;
    }

    public Set<String> getPreferredRoomTags() {
        return preferredRoomTags;
    }

    public Set<String> getProhibitedRoomTags() {
        return prohibitedRoomTags;
    }

    public Set<String> getUndesiredRoomTags() {
        return undesiredRoomTags;
    }

    @Override
    public String toString() {
        return name;
    }
}
