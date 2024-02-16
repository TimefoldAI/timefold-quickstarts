package org.acme.maintenancescheduling.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import org.hibernate.annotations.UuidGenerator;

@Entity
public class Crew {

    @PlanningId
    @Id
    @UuidGenerator
    private String id;

    private String name;

    // No-arg constructor required for Hibernate
    public Crew() {
    }

    public Crew(String name) {
        this.name = name;
    }

    public Crew(String id, String name) {
        this.id = id;
        this.name = name;
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

}
