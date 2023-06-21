package org.acme.vehiclerouting.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private long id;
    @JsonIdentityReference
    private Depot depot;

    @JsonIdentityReference(alwaysAsId = true)
    @PlanningListVariable
    private List<Customer> customers;

    private LocalTime departureTime;

    public Vehicle() {
    }

    public Vehicle(long id, Depot depot, LocalTime departureTime) {
        this.id = id;
        this.depot = depot;
        this.customers = new ArrayList<>();
        this.departureTime = departureTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }
// ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    /**
     * @return route of the vehicle
     */
    public List<Location> getRoute() {
        if (customers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> route = new ArrayList<>();

        route.add(depot.getLocation());
        for (Customer customer : customers) {
            route.add(customer.getLocation());
        }
        route.add(depot.getLocation());

        return route;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public long getTotalDistanceMeters() {
        if (customers.isEmpty()) {
            return 0;
        }

        long totalDistance = 0;
        Location previousLocation = depot.getLocation();

        for (Customer customer : customers) {
            totalDistance += previousLocation.getDistanceTo(customer.getLocation());
            previousLocation = customer.getLocation();
        }
        totalDistance += previousLocation.getDistanceTo(depot.getLocation());

        return totalDistance;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                '}';
    }
}
