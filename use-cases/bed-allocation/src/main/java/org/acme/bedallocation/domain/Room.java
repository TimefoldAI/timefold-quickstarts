package org.acme.bedallocation.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;



@JsonIdentityInfo(scope = Room.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Room {

    @PlanningId
    private String id;

    private String name;

    private Department department;
    private int capacity;
    private GenderLimitation genderLimitation;

    private List<RoomSpecialism> roomSpecialismList;
    private List<String> equipmentList;
    private List<Bed> bedList;

    public Room() {
    }

    public Room(String id, String name, Department department, int capacity, GenderLimitation genderLimitation) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.capacity = capacity;
        this.genderLimitation = genderLimitation;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public GenderLimitation getGenderLimitation() {
        return genderLimitation;
    }

    public void setGenderLimitation(GenderLimitation genderLimitation) {
        this.genderLimitation = genderLimitation;
    }

    public List<RoomSpecialism> getRoomSpecialismList() {
        return roomSpecialismList;
    }

    public void setRoomSpecialismList(List<RoomSpecialism> roomSpecialismList) {
        this.roomSpecialismList = roomSpecialismList;
    }

    public List<String> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<String> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public List<Bed> getBedList() {
        return bedList;
    }

    public void setBedList(List<Bed> bedList) {
        this.bedList = bedList;
    }

}
