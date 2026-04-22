package org.acme.orderpicking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

/**
 * Represents the picking of an order item by a Trolley.
 * <p>
 * The Solver assigns each PickTask to a position in a Trolley's list of picks.
 * Shadow variables provide references to the trolley, the previous pick, and the next pick.
 * <p>
 * Trolley1: [PickTaskA, PickTaskB, PickTaskC]
 * <p>
 * Trolley2: [PickTaskD, PickTaskE]
 */
@PlanningEntity
public class PickTask {

    @PlanningId
    private String id;
    private OrderItem orderItem;

    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "pickTasks")
    private Trolley trolley;

    @JsonIgnore
    @PreviousElementShadowVariable(sourceVariableName = "pickTasks")
    private PickTask previousPickTask;

    @JsonIgnore
    @NextElementShadowVariable(sourceVariableName = "pickTasks")
    private PickTask nextPickTask;

    public PickTask() {
        //marshaling constructor.
    }

    public PickTask(String id, OrderItem orderItem) {
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

    public PickTask getPreviousPickTask() {
        return previousPickTask;
    }

    public void setPreviousPickTask(PickTask previousPickTask) {
        this.previousPickTask = previousPickTask;
    }

    public PickTask getNextPickTask() {
        return nextPickTask;
    }

    public void setNextPickTask(PickTask nextPickTask) {
        this.nextPickTask = nextPickTask;
    }

    public boolean isLast() {
        return nextPickTask == null;
    }

    /**
     * Helper method, facilitates UI building.
     */
    public String getTrolleyId() {
        return trolley != null ? trolley.getId() : null;
    }

    @Override
    public String toString() {
        return "PickTask{" +
                "id='" + id + '\'' +
                ", orderItem=" + orderItem +
                ", trolley=" + trolley +
                '}';
    }
}
