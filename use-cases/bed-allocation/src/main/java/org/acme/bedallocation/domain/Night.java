package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;



@JsonIdentityInfo(scope = Night.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Night {

    @PlanningId
    private String id;
    private int index;

    public Night() {
    }

    public Night(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
