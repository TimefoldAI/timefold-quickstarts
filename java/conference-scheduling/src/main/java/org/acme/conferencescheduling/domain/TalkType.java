package org.acme.conferencescheduling.domain;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = TalkType.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class TalkType {

    private String name;
    private Set<Timeslot> compatibleTimeslots;
    private Set<Room> compatibleRooms;

    public TalkType() {
    }

    public TalkType(String name) {
        this.name = name;
        this.compatibleRooms = new HashSet<>();
        this.compatibleTimeslots = new HashSet<>();
    }

    public void addCompatibleTimeslot(Timeslot timeslot) {
        this.compatibleTimeslots.add(timeslot);
    }

    public void addCompatibleRoom(Room room) {
        this.compatibleRooms.add(room);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Timeslot> getCompatibleTimeslots() {
        return compatibleTimeslots;
    }

    public void setCompatibleTimeslots(Set<Timeslot> compatibleTimeslots) {
        this.compatibleTimeslots = new LinkedHashSet<>(compatibleTimeslots); // Guarantee consistent iteration order.
    }

    public Set<Room> getCompatibleRooms() {
        return compatibleRooms;
    }

    public void setCompatibleRooms(Set<Room> compatibleRooms) {
        this.compatibleRooms = new LinkedHashSet<>(compatibleRooms); // Guarantee consistent iteration order.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TalkType talkType)) return false;
        return Objects.equals(getName(), talkType.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
