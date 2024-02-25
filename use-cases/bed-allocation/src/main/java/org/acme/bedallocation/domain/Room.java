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
    private GenderRoomLimitation genderRoomLimitation;

    private List<RoomSpecialism> roomSpecialismList;
    private List<Equipment> equipmentList;
    private List<Bed> bedList;

    public Room() {
    }

    public Room(String id, String name, Department department, int capacity, GenderRoomLimitation genderRoomLimitation) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.capacity = capacity;
        this.genderRoomLimitation = genderRoomLimitation;
    }

    public int countHardDisallowedStay(Stay stay) {
        return countMissingRequiredRoomProperties(stay.getPatient())
            + department.countHardDisallowedStay(stay)
            + countDisallowedPatientGender(stay.getPatient());
        // TODO preferredMaximumRoomCapacity and specialism
    }

    public int countMissingRequiredRoomProperties(Patient patient) {
        int count = 0;
        for (Equipment requiredEquipment : patient.getRequiredEquipments()) {
            boolean hasRequiredEquipment = false;
            for (Equipment equipment : equipmentList) {
                if (equipment.equals(requiredEquipment)) {
                    hasRequiredEquipment = true;
                }
            }
            if (!hasRequiredEquipment) {
                count += 100000;
            }
        }
        return count;
    }

    public int countDisallowedPatientGender(Patient patient) {
        switch (genderRoomLimitation) {
            case ANY_GENDER:
                return 0;
            case MALE_ONLY:
                return patient.getGender() == Gender.MALE ? 0 : 4;
            case FEMALE_ONLY:
                return patient.getGender() == Gender.FEMALE ? 0 : 4;
            case SAME_GENDER:
                // Constraints check this
                return 1;
            default:
                throw new IllegalStateException("The genderRoomLimitation (" + genderRoomLimitation + ") is not implemented.");
        }
    }

    public int countSoftDisallowedStay(Stay stay) {
        return countMissingPreferredRoomProperties(stay.getPatient());
        // TODO preferredMaximumRoomCapacity and specialism
    }

    public int countMissingPreferredRoomProperties(Patient patient) {
        int count = 0;
        for (Equipment preferredEquipment : patient.getPreferredEquipments()) {
            boolean hasPreferredEquipment = false;
            for (Equipment equipment : equipmentList) {
                if (equipment.equals(preferredEquipment)) {
                    hasPreferredEquipment = true;
                }
            }
            if (!hasPreferredEquipment) {
                count += 20;
            }
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

    public GenderRoomLimitation getGenderRoomLimitation() {
        return genderRoomLimitation;
    }

    public void setGenderRoomLimitation(GenderRoomLimitation genderRoomLimitation) {
        this.genderRoomLimitation = genderRoomLimitation;
    }

    public List<RoomSpecialism> getRoomSpecialismList() {
        return roomSpecialismList;
    }

    public void setRoomSpecialismList(List<RoomSpecialism> roomSpecialismList) {
        this.roomSpecialismList = roomSpecialismList;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public List<Bed> getBedList() {
        return bedList;
    }

    public void setBedList(List<Bed> bedList) {
        this.bedList = bedList;
    }

}
