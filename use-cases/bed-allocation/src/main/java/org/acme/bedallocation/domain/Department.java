package org.acme.bedallocation.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Department.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Department {

    @PlanningId
    private String id;
    private Map<String, Integer> specialtyToPriority;
    private String name;
    private Integer minimumAge = null;
    private Integer maximumAge = null;
    private List<Room> rooms;

    public Department() {
        this.specialtyToPriority = new HashMap<>();
    }

    public Department(String id, String name) {
        this.id = id;
        this.name = name;
        this.specialtyToPriority = new HashMap<>();
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
        return countDisallowedPatientAge(stay.getPatientAge());
    }

    public int countDisallowedPatientAge(int patientAge) {
        int count = 0;
        if (minimumAge != null && patientAge < minimumAge) {
            count += 100;
        }
        if (maximumAge != null && patientAge > maximumAge) {
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

    public Map<String, Integer> getSpecialtyToPriority() {
        return specialtyToPriority;
    }

    public void setSpecialtyToPriority(Map<String, Integer> specialtyToPriority) {
        this.specialtyToPriority = specialtyToPriority;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
