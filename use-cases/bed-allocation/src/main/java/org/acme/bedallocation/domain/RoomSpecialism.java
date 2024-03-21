package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

@JsonIdentityInfo(scope = RoomSpecialism.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RoomSpecialism {

    @PlanningId
    private String id;

    private Room room;
    private Specialism specialism;
    private int priority; // AKA choice

    public RoomSpecialism() {
    }

    public RoomSpecialism(String id, Room room, Specialism specialism) {
        this.id = id;
        this.room = room;
        this.specialism = specialism;
    }

    @Override
    public String toString() {
        return room + "-" + specialism;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Specialism getSpecialism() {
        return specialism;
    }

    public void setSpecialism(Specialism specialism) {
        this.specialism = specialism;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
