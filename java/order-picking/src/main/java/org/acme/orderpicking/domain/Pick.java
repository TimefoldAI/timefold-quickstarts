package org.acme.orderpicking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

/**
 * Represents a "stop" in a Trolley's path where an order item is to be picked.
 * <p>
 * The Solver assigns each Pick to a position in a Trolley's list of picks.
 * Shadow variables provide references to the trolley, the previous pick, and the next pick.
 * <p>
 * Trolley1: [PickA, PickB, PickC]
 * <p>
 * Trolley2: [PickD, PickE]
 */
@PlanningEntity
public class Pick {

    @PlanningId
    private String id;
    private OrderItem orderItem;

    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "picks")
    private Trolley trolley;

    @JsonIgnore
    @PreviousElementShadowVariable(sourceVariableName = "picks")
    private Pick previousPick;

    @JsonIgnore
    @NextElementShadowVariable(sourceVariableName = "picks")
    private Pick nextPick;

    public Pick() {
        //marshaling constructor.
    }

    public Pick(String id, OrderItem orderItem) {
        this.id = id;
        this.orderItem = orderItem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public WarehouseLocation getLocation() {
        return orderItem.getProduct().getLocation();
    }

    public Trolley getTrolley() {
        return trolley;
    }

    public void setTrolley(Trolley trolley) {
        this.trolley = trolley;
    }

    public Pick getPreviousPick() {
        return previousPick;
    }

    public void setPreviousPick(Pick previousPick) {
        this.previousPick = previousPick;
    }

    public Pick getNextPick() {
        return nextPick;
    }

    public void setNextPick(Pick nextPick) {
        this.nextPick = nextPick;
    }

    public boolean isLast() {
        return nextPick == null;
    }

    /**
     * Helper method, facilitates UI building.
     */
    public String getTrolleyId() {
        return trolley != null ? trolley.getId() : null;
    }

}
