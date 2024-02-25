package org.acme.bedallocation.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class DepartmentSpecialism {

    @PlanningId
    private String id;

    private Department department;
    private Specialism specialism;

    private int priority; // AKA choice

    public DepartmentSpecialism() {
    }

    public DepartmentSpecialism(String id, Department department, Specialism specialism, int priority) {
        this.id = id;
        this.department = department;
        this.specialism = specialism;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return department + "-" + specialism;
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

    public Specialism getSpecialism() {
        return specialism;
    }

    public void setSpecialism(Specialism specialism) {
        this.specialism = specialism;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
