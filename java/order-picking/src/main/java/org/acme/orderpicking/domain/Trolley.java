package org.acme.orderpicking.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

/**
 * Represents the trolley that will be filled with the order items.
 *
 * @see PickTask for more information about the model constructed by the Solver.
 */
@PlanningEntity
public class Trolley {

    @PlanningId
    private String id;
    private int bucketCount;
    private int bucketCapacity;
    private WarehouseLocation location;

    @PlanningListVariable
    private List<PickTask> pickTasks = new ArrayList<>();

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

    public WarehouseLocation getLocation() {
        return location;
    }

    public void setLocation(WarehouseLocation location) {
        this.location = location;
    }

    public List<PickTask> getPickTasks() {
        return pickTasks;
    }

    public void setPickTasks(List<PickTask> pickTasks) {
        this.pickTasks = pickTasks;
    }
}
