package org.acme.bedallocation.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;


@JsonIdentityInfo(scope = Patient.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Patient {

    @PlanningId
    private String id;

    private String name;
    private Gender gender;
    private int age;
    private Integer preferredMaximumRoomCapacity;

    private List<String> requiredEquipmentList;
    private List<String> preferredEquipmentList;

    public Patient() {
    }

    public Patient(String id, String name, Gender gender, int age, Integer preferredMaximumRoomCapacity) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.preferredMaximumRoomCapacity = preferredMaximumRoomCapacity;
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

    public List<String> getRequiredEquipmentList() {
        return requiredEquipmentList;
    }

    public void setRequiredEquipmentList(List<String> requiredEquipmentList) {
        this.requiredEquipmentList = requiredEquipmentList;
    }

    public List<String> getPreferredEquipmentList() {
        return preferredEquipmentList;
    }

    public void setPreferredEquipmentList(List<String> preferredEquipmentList) {
        this.preferredEquipmentList = preferredEquipmentList;
    }

}
