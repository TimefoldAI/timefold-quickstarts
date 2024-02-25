package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;


@JsonIdentityInfo(scope = RoomEquipment.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RoomEquipment {

    @PlanningId
    private String id;

    private Room room;
    private Equipment equipment;

    public RoomEquipment() {
    }

    public RoomEquipment(String id, Room room, Equipment equipment) {
        this.id = id;
        this.room = room;
        this.equipment = equipment;
    }

    @Override
    public String toString() {
        return room + "-" + equipment;
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

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

}
