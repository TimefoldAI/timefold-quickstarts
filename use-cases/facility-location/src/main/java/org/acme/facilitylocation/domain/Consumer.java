package org.acme.facilitylocation.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import org.acme.facilitylocation.solver.FacilityLocationConstraintProvider;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Consumer has a demand that can be satisfied by <em>any</em> {@link Facility} with a sufficient capacity.
 * <p/>
 * Closer facilities are preferred as the distance affects travel time, signal quality, etc.
 * This requirement is expressed by the
 * {@link FacilityLocationConstraintProvider#distanceFromFacility distance from facility} constraint.
 * <p/>
 * One of the FLP's goals is to minimize total set-up cost by selecting cheaper facilities. This requirement
 * is expressed by the {@link FacilityLocationConstraintProvider#setupCost setup cost} constraint.
 */
@PlanningEntity
public class Consumer {

    @PlanningId
    private String id;
    private Location location;
    private long demand;

    @PlanningVariable
    private Facility facility;

    public Consumer() {
    }

    public Consumer(String id, Location location, long demand) {
        this.id = id;
        this.location = location;
        this.demand = demand;
    }

    public boolean isAssigned() {
        return facility != null;
    }

    /**
     * Get distance from the facility.
     *
     * @return distance in meters
     */
    public long distanceFromFacility() {
        if (facility == null) {
            throw new IllegalStateException("No facility is assigned.");
        }
        return facility.getLocation().getDistanceTo(location);
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

    public long getDemand() {
        return demand;
    }

    public void setDemand(long demand) {
        this.demand = demand;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return "Consumer " + id + " (" + demand + " dem)";
    }
}
