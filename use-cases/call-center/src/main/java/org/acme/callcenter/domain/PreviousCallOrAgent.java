package org.acme.callcenter.domain;

import java.time.Duration;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public abstract class PreviousCallOrAgent {

    @PlanningId
    private String id;

    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "previousCallOrAgent")
    protected Call nextCall;

    public PreviousCallOrAgent() {
        // Required by Timefold.
    }

    public PreviousCallOrAgent(String id) {
        this.id = id;
    }

    public Call getNextCall() {
        return nextCall;
    }

    public void setNextCall(Call nextCall) {
        this.nextCall = nextCall;
    }

    public abstract Duration getDurationTillPickUp();

    public String getId() {
        return id;
    }
}
