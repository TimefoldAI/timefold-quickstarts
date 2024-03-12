package org.acme.orderpicking.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Represents the trolley that will be filled with the order items.
 * 
 * @see TrolleyStep for more information about the model constructed by the Solver.
 */
public class Trolley extends TrolleyOrTrolleyStep {

    @PlanningId
    private String id;
    private int bucketCount;
    private int bucketCapacity;
    private WarehouseLocation location;

    public Trolley() {
        //marshalling constructor
    }

    public Trolley(String id, int bucketCount, int bucketCapacity, WarehouseLocation location) {
        this.id = id;
        this.bucketCount = bucketCount;
        this.bucketCapacity = bucketCapacity;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBucketCount() {
        return bucketCount;
    }

    public void setBucketCount(int bucketCount) {
        this.bucketCount = bucketCount;
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public void setBucketCapacity(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    @Override
    public WarehouseLocation getLocation() {
        return location;
    }

    public void setLocation(WarehouseLocation location) {
        this.location = location;
    }
}
