package org.acme.vehiclerouting.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Vehicle.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Vehicle {

    @PlanningId
    private long id;
    private int capacity;
    @JsonIdentityReference
    private Depot depot;

    @JsonIdentityReference
    @PlanningListVariable
    private List<Customer> customerList;

    public Vehicle() {
    }

    public Vehicle(long id, int capacity, Depot depot) {
        this.id = id;
        this.capacity = capacity;
        this.depot = depot;
        this.customerList = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @return route of the vehicle
     */
    public List<Location> getRoute() {
        if (customerList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> route = new ArrayList<Location>();

        route.add(depot.getLocation());
        for (Customer customer : customerList) {
            route.add(customer.getLocation());
        }
        route.add(depot.getLocation());

        return route;
    }

    public int getTotalDemand() {
        int totalDemand = 0;
        for (Customer customer : customerList) {
            totalDemand += customer.getDemand();
        }
        return totalDemand;
    }

    public long getTotalDistanceMeters() {
        if (customerList.isEmpty()) {
            return 0;
        }

        long totalDistance = 0;
        Location previousLocation = depot.getLocation();

        for (Customer customer : customerList) {
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
