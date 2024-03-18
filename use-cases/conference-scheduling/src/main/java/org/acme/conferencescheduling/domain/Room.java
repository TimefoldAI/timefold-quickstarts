package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

public class Room {

    private String id;
    private String name;
    private int capacity;

    private Set<TalkType> talkTypes;
    private Set<Timeslot> unavailableTimeslots;
    private Set<String> tags;

    public Room() {
    }

    public Room(String id) {
        this(id, id, 0, emptySet(), emptySet());
    }

    public Room(String id, String name) {
        this(id, name, 0, emptySet(), emptySet(), emptySet());
    }

    public Room(String id, String name, int capacity, Set<TalkType> talkTypes, Set<String> tags) {
        this(id, name, capacity, talkTypes, emptySet(), tags);
    }

    public Room(String id, Set<Timeslot> unavailableTimeslots) {
        this(id);
        this.unavailableTimeslots = unavailableTimeslots;
    }

    public Room(String id, String name, int capacity, Set<TalkType> talkTypes, Set<Timeslot> unavailableTimeslots,
            Set<String> tags) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.talkTypes = talkTypes;
        this.unavailableTimeslots = unavailableTimeslots;
        this.tags = tags;
        talkTypes.forEach(t -> t.addCompatibleRoom(this));
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
        return Objects.equals(getId(), room.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
