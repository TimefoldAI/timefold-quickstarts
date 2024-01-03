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
    private Depot depot;

    private LocalDateTime departureTime;

    @JsonIdentityReference(alwaysAsId = true)
    @PlanningListVariable
    private List<Customer> customers;

    public Vehicle() {
    }

    public Vehicle(String id, int capacity, Depot depot, LocalDateTime departureTime) {
        this.id = id;
        this.capacity = capacity;
        this.depot = depot;
        this.departureTime = departureTime;
        this.customers = new ArrayList<>();
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

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getTotalDemand() {
        int totalDemand = 0;
        for (Customer customer : customers) {
            totalDemand += customer.getDemand();
        }
        return totalDemand;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public long getTotalDrivingTimeSeconds() {
        if (customers.isEmpty()) {
            return 0;
        }

        long totalDrivingTime = 0;
        Location previousLocation = depot.getLocation();

        for (Customer customer : customers) {
            totalDrivingTime += previousLocation.getDrivingTimeTo(customer.getLocation());
            previousLocation = customer.getLocation();
        }
        totalDrivingTime += previousLocation.getDrivingTimeTo(depot.getLocation());

        return totalDrivingTime;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime arrivalTime() {
        if (customers.isEmpty()) {
            return departureTime;
        }

        Customer lastCustomer = customers.get(customers.size()-1);
        return lastCustomer.getDepartureTime().plusSeconds(lastCustomer.getLocation().getDrivingTimeTo(depot.getLocation()));
    }

    @Override
    public String toString() {
        return id;
    }

}
