package org.acme.bedallocation.domain;

import java.util.LinkedList;
import java.util.List;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Patient.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Patient {

    @PlanningId
    private String id;

    private String name;
    private Gender gender;
    private int age;
    private Integer preferredMaximumRoomCapacity;

    private List<String> requiredEquipments;
    private List<String> preferredEquipments;

    public Patient() {
        this.requiredEquipments = new LinkedList<>();
        this.preferredEquipments = new LinkedList<>();
    }

    public Patient(String id, String name) {
        this.id = id;
        this.name = name;
        this.age = -1;
        this.requiredEquipments = new LinkedList<>();
        this.preferredEquipments = new LinkedList<>();
    }

    public Patient(String id, String name, Gender gender, int age, Integer preferredMaximumRoomCapacity) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.preferredMaximumRoomCapacity = preferredMaximumRoomCapacity;
        this.requiredEquipments = new LinkedList<>();
        this.preferredEquipments = new LinkedList<>();
    }

    public void addRequiredEquipment(String equipment) {
        if (!requiredEquipments.contains(equipment)) {
            this.requiredEquipments.add(equipment);
        }
    }

    public void addPreferredEquipment(String equipment) {
        if (!preferredEquipments.contains(equipment)) {
            this.preferredEquipments.add(equipment);
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Integer getPreferredMaximumRoomCapacity() {
        return preferredMaximumRoomCapacity;
    }

    public void setPreferredMaximumRoomCapacity(Integer preferredMaximumRoomCapacity) {
        this.preferredMaximumRoomCapacity = preferredMaximumRoomCapacity;
    }

    public List<String> getRequiredEquipments() {
        return requiredEquipments;
    }

    public void setRequiredEquipments(List<String> requiredEquipments) {
        this.requiredEquipments = requiredEquipments;
    }

    public List<String> getPreferredEquipments() {
        return preferredEquipments;
    }

    public void setPreferredEquipments(List<String> preferredEquipments) {
        this.preferredEquipments = preferredEquipments;
    }

}
