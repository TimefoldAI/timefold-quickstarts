package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;


/**
 * AKA RoomProperty.
 */
@JsonIdentityInfo(scope = Equipment.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Equipment {

    @PlanningId
    private String id;

    private String name;

    public Equipment() {
    }

    public Equipment(String id, String name) {
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

    public void setName(String name) {
        this.name = name;
    }

}
