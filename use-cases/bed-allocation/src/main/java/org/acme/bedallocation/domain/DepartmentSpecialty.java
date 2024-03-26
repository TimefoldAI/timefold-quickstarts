package org.acme.bedallocation.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = DepartmentSpecialty.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DepartmentSpecialty {

    @PlanningId
    private String id;

    private Department department;
    private String specialty;

    private int priority; // AKA choice

    public DepartmentSpecialty() {
    }

    public DepartmentSpecialty(String id, Department department, String specialty, int priority) {
        this.id = id;
        this.department = department;
        this.specialty = specialty;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return department + "-" + specialty;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
