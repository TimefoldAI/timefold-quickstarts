package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;



@JsonIdentityInfo(scope = Bed.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Bed {

    @PlanningId
    private String id;

    private Room room;
    private int indexInRoom;

    public Bed() {
    }

    public Bed(String id, Room room, int indexInRoom) {
        this.id = id;
        this.room = room;
        this.indexInRoom = indexInRoom;
    }

    @Override
    public String toString() {
        return room + "(" + indexInRoom + ")";
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

    public int getIndexInRoom() {
        return indexInRoom;
    }

    public void setIndexInRoom(int indexInRoom) {
        this.indexInRoom = indexInRoom;
    }

}
