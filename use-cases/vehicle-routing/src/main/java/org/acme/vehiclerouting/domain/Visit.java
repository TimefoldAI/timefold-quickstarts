package org.acme.vehiclerouting.domain;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.service.maps.api.model.Location;

@PlanningEntity
public class Visit implements LocationAware {

    @PlanningId
    private String id;
    private String name;
    private Location location;
    private int demand;
    private OffsetDateTime minStartTime;
    private OffsetDateTime maxEndTime;
    private Duration serviceDuration;

    @InverseRelationShadowVariable(sourceVariableName = "visits")
    private Vehicle vehicle;
    @PreviousElementShadowVariable(sourceVariableName = "visits")
    private Visit previousVisit;
    @ShadowVariable(supplierName = "arrivalTimeSupplier")
    private OffsetDateTime arrivalTime;

    public Visit() {
    }

    public Visit(String id, String name, Location location, int demand,
            OffsetDateTime minStartTime, OffsetDateTime maxEndTime, Duration serviceDuration) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.demand = demand;
        this.minStartTime = minStartTime;
        this.maxEndTime = maxEndTime;
        this.serviceDuration = serviceDuration;
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
        if (!(o instanceof Visit visit)) {
            return false;
        }
        return Objects.equals(id, visit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @SuppressWarnings("unused")
    @ShadowSources({ "vehicle", "previousVisit.arrivalTime" })
    public OffsetDateTime arrivalTimeSupplier() {
        if (previousVisit == null && vehicle == null) {
            return null;
        }
        OffsetDateTime departureTime = previousVisit == null ? vehicle.getDepartureTime() : previousVisit.getDepartureTime();
        return departureTime != null ? departureTime.plusSeconds(getDrivingTimeSecondsFromPreviousStandstill()) : null;
    }

    public OffsetDateTime getDepartureTime() {
        if (arrivalTime == null) {
            return null;
        }
        return getStartServiceTime().plus(serviceDuration);
    }

    /**
     * @return the time servicing actually starts: a vehicle that arrives before the visit is ready
     *         waits until it is
     */
    public OffsetDateTime getStartServiceTime() {
        if (arrivalTime == null) {
            return null;
        }
        return arrivalTime.isBefore(minStartTime) ? minStartTime : arrivalTime;
    }

    public boolean isAssigned() {
        return vehicle != null;
    }

    public boolean isServiceFinishedAfterMaxEndTime() {
        return arrivalTime != null
                && arrivalTime.plus(serviceDuration).isAfter(maxEndTime);
    }

    public long getServiceFinishedDelayInMinutes() {
        if (arrivalTime == null) {
            return 0;
        }
        return roundDurationToNextOrEqualMinutes(Duration.between(maxEndTime, arrivalTime.plus(serviceDuration)));
    }

    private static long roundDurationToNextOrEqualMinutes(Duration duration) {
        var remainder = duration.minus(duration.truncatedTo(ChronoUnit.MINUTES));
        var minutes = duration.toMinutes();
        if (remainder.equals(Duration.ZERO)) {
            return minutes;
        }
        return minutes + 1;
    }

    public long getDrivingTimeSecondsFromPreviousStandstill() {
        if (vehicle == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (previousVisit == null) {
            return vehicle.getHomeLocation().getDrivingTimeTo(location).seconds();
        }
        return previousVisit.getLocation().getDrivingTimeTo(location).seconds();
    }

    /**
     * @return the same driving time as {@link #getDrivingTimeSecondsFromPreviousStandstill()}, but
     *         null instead of an exception while this visit is still unassigned - the web UI draws
     *         the travel block of a route from it
     */
    public Long getDrivingTimeSecondsFromPreviousStandstillOrNull() {
        if (vehicle == null) {
            return null;
        }
        return getDrivingTimeSecondsFromPreviousStandstill();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public OffsetDateTime getMinStartTime() {
        return minStartTime;
    }

    public OffsetDateTime getMaxEndTime() {
        return maxEndTime;
    }

    public Duration getServiceDuration() {
        return serviceDuration;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Visit getPreviousVisit() {
        return previousVisit;
    }

    public void setPreviousVisit(Visit previousVisit) {
        this.previousVisit = previousVisit;
    }

    public OffsetDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(OffsetDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

}
