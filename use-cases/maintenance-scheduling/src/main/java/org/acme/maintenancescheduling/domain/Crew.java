package org.acme.maintenancescheduling.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Crew {

    @PlanningId
    private String id;

    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Crew crew)) {
            return false;
        }
        return Objects.equals(getId(), crew.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
