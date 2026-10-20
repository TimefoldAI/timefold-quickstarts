package org.acme.orderpicking.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

/**
 * Represents the picking cart that will be filled with the order items. It starts at its location, walks past every
 * {@link PickTask} in its list in that order, and returns to its location.
 * <p>
 * A picking cart holds {@link #getBucketCount() a number of buckets} of {@link #getBucketCapacity() the same capacity},
 * and a bucket never holds products of two different orders.
 *
 * @see PickTask for more information about the model constructed by the Solver.
 */
@PlanningEntity
public class PickingCart {

    @PlanningId
    private String id;
    private int bucketCount;
    private int bucketCapacity;
    private WarehouseLocation location;

    @PlanningListVariable
    private List<PickTask> pickTasks = new ArrayList<>();

    public PickingCart() {
    }

    public PickingCart(String id, int bucketCount, int bucketCapacity, WarehouseLocation location) {
        this.id = id;
        this.bucketCount = bucketCount;
        this.bucketCapacity = bucketCapacity;
        this.location = location;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public int getBucketCount() {
        return bucketCount;
    }

    /**
     * @return the volume of one bucket in cm3
     */
    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public WarehouseLocation getLocation() {
        return location;
    }

    public List<PickTask> getPickTasks() {
        return pickTasks;
    }

    public void setPickTasks(List<PickTask> pickTasks) {
        this.pickTasks = pickTasks;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @param orderVolume the volume in cm3 of all the products of one order this picking cart picks
     * @return the number of buckets those products take up, as a bucket is never shared with another order
     */
    public long calculateRequiredBuckets(long orderVolume) {
        return (orderVolume + (bucketCapacity - 1)) / bucketCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PickingCart pickingCart)) {
            return false;
        }
        return Objects.equals(id, pickingCart.id);
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
