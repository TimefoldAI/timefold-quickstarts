package org.acme.facilitylocation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.service.maps.api.model.Location;

/**
 * Facility satisfies consumers' demand. Cumulative demand of all consumers assigned to this facility must not exceed
 * the facility's capacity. This requirement is expressed by the facility capacity constraint
 * (see {@code FacilityLocationConstraintProvider}).
 */
// This is a shadow planning entity, not a genuine planning entity, because it has a shadow variable (consumers).
@PlanningEntity
public class Facility {

    @PlanningId
    private String id;
    private Location location;
    private long setupCost;
    private long capacity;

    @InverseRelationShadowVariable(sourceVariableName = "facility")
    private List<Consumer> consumers = new ArrayList<>();

    public Facility() {
    }

    public Facility(String id, Location location, long setupCost, long capacity) {
        this.id = id;
        this.location = location;
        this.setupCost = setupCost;
        this.capacity = capacity;
    }

    /**
     * The cumulative demand of every consumer assigned to this facility. Reading the
     * {@link InverseRelationShadowVariable} is safe here: the Service module runs the solver model through
     * {@code SolutionManager.update(...)} before it converts it to a model output, so the inverse relation is
     * always up to date by then.
     *
     * @return the used capacity, never negative
     */
    public long getUsedCapacity() {
        return consumers.stream().mapToLong(Consumer::getDemand).sum();
    }

    /**
     * @return true if at least one consumer is served by this facility, so its setup cost has to be paid
     */
    public boolean isUsed() {
        return !consumers.isEmpty();
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getSetupCost() {
        return setupCost;
    }

    public void setSetupCost(long setupCost) {
        this.setupCost = setupCost;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Facility facility)) {
            return false;
        }
        return Objects.equals(id, facility.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Facility " + id;
    }
}
