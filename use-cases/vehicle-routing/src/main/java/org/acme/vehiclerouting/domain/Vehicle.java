package org.acme.vehiclerouting.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Vehicle.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Vehicle {

    @PlanningId
    private String id;
    private int capacity;
    @JsonIdentityReference
    private Location homeLocation;

    private LocalDateTime departureTime;

    @JsonIdentityReference(alwaysAsId = true)
    @PlanningListVariable
    private List<Visit> visits;

    public Vehicle() {
    }

    public Vehicle(String id, int capacity, Location homeLocation, LocalDateTime departureTime) {
        this.id = id;
        this.capacity = capacity;
        this.homeLocation = homeLocation;
        this.departureTime = departureTime;
        this.visits = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getTotalDemand() {
        int totalDemand = 0;
        for (Visit visit : visits) {
            totalDemand += visit.getDemand();
        }
        return totalDemand;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public long getTotalDrivingTimeSeconds() {
        if (visits.isEmpty()) {
            return 0;
        }

        long totalDrivingTime = 0;
        Location previousLocation = homeLocation;

        for (Visit visit : visits) {
            totalDrivingTime += previousLocation.getDrivingTimeTo(visit.getLocation());
            previousLocation = visit.getLocation();
        }
        totalDrivingTime += previousLocation.getDrivingTimeTo(homeLocation);

        return totalDrivingTime;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime arrivalTime() {
        if (visits.isEmpty()) {
            return departureTime;
        }

        Visit lastVisit = visits.get(visits.size() - 1);
        return lastVisit.getDepartureTime().plusSeconds(lastVisit.getLocation().getDrivingTimeTo(homeLocation));
    }

    @Override
    public String toString() {
        return id;
    }

}
