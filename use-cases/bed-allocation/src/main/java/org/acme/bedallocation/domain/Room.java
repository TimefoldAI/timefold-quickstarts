package org.acme.bedallocation.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Room.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Room {

    @PlanningId
    private String id;
    private String name;
    private Department department;
    private int capacity;
    private GenderLimitation genderLimitation;
    private List<String> equipments;
    private List<Bed> beds;

    public Room() {
        this.equipments = new LinkedList<>();
        this.beds = new LinkedList<>();
    }

    public Room(String id) {
        this.id = id;
        this.name = id;
        this.equipments = new LinkedList<>();
        this.beds = new LinkedList<>();
    }

    public Room(String id, String name, Department department) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.department.addRoom(this);
        this.equipments = new LinkedList<>();
        this.beds = new LinkedList<>();
    }

    public void addBed(Bed bed) {
        if (!beds.contains(bed)) {
            beds.add(bed);
        }
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

    public List<String> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<String> equipments) {
        this.equipments = equipments;
    }

    public List<Bed> getBeds() {
        return beds;
    }

    public void setBeds(List<Bed> beds) {
        this.beds = beds;
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
        return getId().hashCode();
    }
}
