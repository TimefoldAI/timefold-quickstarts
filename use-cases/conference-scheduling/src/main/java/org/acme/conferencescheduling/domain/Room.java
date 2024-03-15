package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

public class Room {

    private String name;
    private int capacity;

    private Set<TalkType> talkTypes;
    private Set<Timeslot> unavailableTimeslots;
    private Set<String> tags;

    public Room() {
    }

    public Room(String name) {
        this(name, 0, emptySet(), emptySet());
    }

    public Room(String name, int capacity, Set<TalkType> talkTypes, Set<String> tags) {
        this(name, capacity, talkTypes, emptySet(), tags);
    }

    public Room(String name, Set<Timeslot> unavailableTimeslots) {
        this(name);
        this.unavailableTimeslots = unavailableTimeslots;
    }

    public Room(String name, int capacity, Set<TalkType> talkTypes, Set<Timeslot> unavailableTimeslots,
            Set<String> tags) {
        this.name = name;
        this.capacity = capacity;
        this.talkTypes = talkTypes;
        this.unavailableTimeslots = unavailableTimeslots;
        this.tags = tags;
        talkTypes.forEach(t -> t.addCompatibleRoom(this));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Set<TalkType> getTalkTypes() {
        return talkTypes;
    }

    public void setTalkTypes(Set<TalkType> talkTypes) {
        this.talkTypes = talkTypes;
    }

    public Set<Timeslot> getUnavailableTimeslots() {
        return unavailableTimeslots;
    }

    public void setUnavailableTimeslots(Set<Timeslot> unavailableTimeslots) {
        this.unavailableTimeslots = unavailableTimeslots;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Room room))
            return false;
        return Objects.equals(getName(), room.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
