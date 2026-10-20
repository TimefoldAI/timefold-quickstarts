package org.acme.orderpicking.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

/**
 * Represents the picking of one order item, a {@link Product} that was added to an order, by a {@link PickingCart}.
 * <p>
 * The Solver assigns each PickTask to a position in a picking cart's list of picks.
 * Shadow variables provide references to the picking cart, the previous pick, and the next pick.
 * <p>
 * PickingCart1: [PickTaskA, PickTaskB, PickTaskC]
 * <p>
 * PickingCart2: [PickTaskD, PickTaskE]
 */
@PlanningEntity
public class PickTask {

    @PlanningId
    private String id;
    private String orderId;
    private Product product;

    @InverseRelationShadowVariable(sourceVariableName = "pickTasks")
    private PickingCart pickingCart;
    @PreviousElementShadowVariable(sourceVariableName = "pickTasks")
    private PickTask previousPickTask;
    @NextElementShadowVariable(sourceVariableName = "pickTasks")
    private PickTask nextPickTask;

    public PickTask() {
    }

    public PickTask(String id, String orderId, Product product) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Product getProduct() {
        return product;
    }

    public PickingCart getPickingCart() {
        return pickingCart;
    }

    public void setPickingCart(PickingCart pickingCart) {
        this.pickingCart = pickingCart;
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

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public WarehouseLocation getLocation() {
        return product.location();
    }

    public int getVolume() {
        return product.volume();
    }

    public boolean isLast() {
        return nextPickTask == null;
    }

    /**
     * @return the location the picking cart comes from to pick this task: the previous pick task's, or the picking
     *         cart's own location for the first pick task of its list
     */
    public WarehouseLocation getPreviousLocation() {
        return previousPickTask != null ? previousPickTask.getLocation() : pickingCart.getLocation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PickTask pickTask)) {
            return false;
        }
        return Objects.equals(id, pickTask.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
