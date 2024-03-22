package org.acme.bedallocation.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = RoomSpecialism.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RoomSpecialism {

    @PlanningId
    private String id;

    private Room room;
    private String specialism;
    private int priority; // AKA choice

    public RoomSpecialism() {
    }

    public RoomSpecialism(String id, Room room, String specialism, int priority) {
        this.id = id;
        this.room = room;
        this.specialism = specialism;
        this.priority = priority;
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

    public String getSpecialism() {
        return specialism;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
