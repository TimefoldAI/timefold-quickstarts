package org.acme.bedallocation.domain;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

@JsonIdentityInfo(scope = Department.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Department {

    @PlanningId
    private String id;

    private String name;
    private Integer minimumAge = null;
    private Integer maximumAge = null;

    private List<Room> rooms;

    public Department() {
    }

    public Department(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addRoom(Room room) {
        if (rooms == null) {
            rooms = new LinkedList<>();
        }
        if (!rooms.contains(room)) {
            rooms.add(room);
        }
    }

    public int countHardDisallowedStay(Stay stay) {
        return countDisallowedPatientAge(stay.getPatient());
    }

    public int countDisallowedPatientAge(Patient patient) {
        int count = 0;
        if (minimumAge != null && patient.getAge() < minimumAge) {
            count += 100;
        }
        if (maximumAge != null && patient.getAge() > maximumAge) {
            count += 100;
        }
        return count;
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

    public Integer getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(Integer minimumAge) {
        this.minimumAge = minimumAge;
    }

    public Integer getMaximumAge() {
        return maximumAge;
    }

    public void setMaximumAge(Integer maximumAge) {
        this.maximumAge = maximumAge;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

}
