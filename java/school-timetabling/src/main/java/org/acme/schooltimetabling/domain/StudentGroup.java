package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class StudentGroup {

    @PlanningId
    private String id; // <-- We need an ID for the @PlanningId
    private String name;

    public StudentGroup() {
    }

    // --- THIS IS THE CORRECTED CONSTRUCTOR ---
    public StudentGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}