package org.acme.vehiclerouting.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.service.maps.api.model.Location;

@PlanningEntity
public class Vehicle implements LocationAware {

    @PlanningId
    private String id;
    private int capacity;
    private Location homeLocation;
    private OffsetDateTime departureTime;

    /**
     * The route of this vehicle: the visits it services, in the order it services them. The
     * assignment <em>is</em> this list, so a visit that appears in no vehicle's list is unassigned.
     */
    @PlanningListVariable(allowsUnassignedValues = true)
    private List<Visit> visits;

    public Vehicle() {
    }

    public Vehicle(String id, int capacity, Location homeLocation, OffsetDateTime departureTime) {
        this.id = id;
        this.capacity = capacity;
        this.homeLocation = homeLocation;
        this.departureTime = departureTime;
        this.visits = new ArrayList<>();
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle vehicle)) {
            return false;
        }
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public Location getLocation() {
        return homeLocation;
    }

    public int getTotalDemand() {
        int totalDemand = 0;
        for (Visit visit : visits) {
            totalDemand += visit.getDemand();
        }
        return totalDemand;
    }

    /**
     * @return the driving time of the whole route, home location to home location, in seconds
     */
    public long getTotalDrivingTimeSeconds() {
        if (visits.isEmpty()) {
            return 0;
        }

        long totalDrivingTime = 0;
        Location previousLocation = homeLocation;

        for (Visit visit : visits) {
            totalDrivingTime += previousLocation.getTravelTimeTo(visit.getLocation()).seconds();
            previousLocation = visit.getLocation();
        }
        totalDrivingTime += previousLocation.getTravelTimeTo(homeLocation).seconds();

        return totalDrivingTime;
    }

    /**
     * @return the time this vehicle is back at its home location, or its departure time when it has
     *         no visits to make; null while the arrival time shadow of its last visit is not
     *         computed yet
     */
    public OffsetDateTime arrivalTime() {
        if (visits.isEmpty()) {
            return departureTime;
        }

        Visit lastVisit = visits.get(visits.size() - 1);
        OffsetDateTime lastDepartureTime = lastVisit.getDepartureTime();
        if (lastDepartureTime == null) {
            return null;
        }
        return lastDepartureTime.plusSeconds(lastVisit.getLocation().getTravelTimeTo(homeLocation).seconds());
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public OffsetDateTime getDepartureTime() {
        return departureTime;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

}
