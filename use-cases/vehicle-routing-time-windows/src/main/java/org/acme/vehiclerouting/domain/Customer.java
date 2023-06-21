package org.acme.vehiclerouting.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import org.acme.vehiclerouting.solver.ArrivalTimeUpdatingVariableListener;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Customer.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Customer {

    private long id;
    @JsonIdentityReference
    private Location location;
    private LocalDateTime readyTime;
    private LocalDateTime dueTime;
    private Duration serviceDuration;

    private Vehicle vehicle;

    private Customer previousCustomer;

    private Customer nextCustomer;

    private LocalDateTime arrivalTime;

    public Customer() {
    }

    public Customer(long id, Location location, LocalDateTime readyTime, LocalDateTime dueTime, Duration serviceDuration) {
        this.id = id;
        this.readyTime = readyTime;
        this.dueTime = dueTime;
        this.serviceDuration = serviceDuration;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getReadyTime() {
        return readyTime;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public Duration getServiceDuration() {
        return serviceDuration;
    }

    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "customers")
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @JsonIgnore
    @PreviousElementShadowVariable(sourceVariableName = "customers")
    public Customer getPreviousCustomer() {
        return previousCustomer;
    }

    public void setPreviousCustomer(Customer previousCustomer) {
        this.previousCustomer = previousCustomer;
    }

    @JsonIgnore
    @NextElementShadowVariable(sourceVariableName = "customers")
    public Customer getNextCustomer() {
        return nextCustomer;
    }

    public void setNextCustomer(Customer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    @ShadowVariable(variableListenerClass = ArrivalTimeUpdatingVariableListener.class, sourceVariableName = "vehicle")
    @ShadowVariable(variableListenerClass = ArrivalTimeUpdatingVariableListener.class, sourceVariableName = "previousCustomer")
    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public LocalDateTime getDepartureTime() {
        if (arrivalTime == null) {
            return null;
        }
        LocalDateTime startServiceTime = arrivalTime.isBefore(readyTime) ? readyTime : arrivalTime;
        return startServiceTime.plus(serviceDuration);
    }

    @JsonIgnore
    public boolean isArrivalBeforeReadyTime() {
        return arrivalTime != null
                && arrivalTime.isBefore(readyTime);
    }

    @JsonIgnore
    public boolean isArrivalAfterDueTime() {
        return arrivalTime != null
                && arrivalTime.isAfter(dueTime);
    }

    @JsonIgnore
    public boolean isServiceFinishedAfterDueTime() {
        return arrivalTime != null
                && arrivalTime.plus(serviceDuration).isAfter(dueTime);
    }

    @JsonIgnore
    public long getServiceFinishedDelayInMinutes() {
        if (arrivalTime == null) {
            return 0;
        }
        return Duration.between(dueTime, arrivalTime.plus(serviceDuration)).toMinutes();
    }

    @JsonIgnore
    public long getDistanceFromPreviousStandstill() {
        if (vehicle == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (previousCustomer == null) {
            return vehicle.getDepot().getLocation().getDistanceTo(location);
        }
        return previousCustomer.getLocation().getDistanceTo(location);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                '}';
    }
}
