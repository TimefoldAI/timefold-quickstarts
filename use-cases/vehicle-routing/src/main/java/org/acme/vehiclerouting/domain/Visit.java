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
    @ShadowVariable(supplierName = "timingsSupplier")
    private Timings timings;
    @ShadowVariable(supplierName = "cumulativeDemandSupplier")
    private Integer cumulativeDemand;

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

    /**
     * Computes the arrival, start service and departure time and the cumulative driving time in one
     * go: they are derived from the same predecessor's departure time, so a single supplier keeps
     * them consistent and avoids recomputing the chain several times.
     *
     * @return null while this visit is unassigned or its predecessor is not timed yet
     */
    @SuppressWarnings("unused")
    @ShadowSources({ "vehicle", "previousVisit.timings" })
    public Timings timingsSupplier() {
        if (previousVisit == null && vehicle == null) {
            return null;
        }
        OffsetDateTime previousDepartureTime =
                previousVisit == null ? vehicle.getDepartureTime() : previousVisit.getDepartureTime();
        if (previousDepartureTime == null) {
            return null;
        }
        long drivingTimeSeconds = getDrivingTimeSecondsFromPreviousStandstill();
        long previousCumulativeDrivingTimeSeconds =
                previousVisit == null ? 0 : previousVisit.getCumulativeDrivingTimeSeconds();
        var arrivalTime = previousDepartureTime.plusSeconds(drivingTimeSeconds);
        var startServiceTime = arrivalTime.isBefore(minStartTime) ? minStartTime : arrivalTime;
        return new Timings(arrivalTime, startServiceTime, startServiceTime.plus(serviceDuration),
                previousCumulativeDrivingTimeSeconds + drivingTimeSeconds);
    }

    /**
     * Kept apart from {@link Timings} because it does not depend on them: a change that only affects
     * the timings does not recalculate the demand.
     *
     * @return the demand of the whole route up to and including this visit; null while this visit is
     *         unassigned or its predecessor's demand is not computed yet
     */
    @SuppressWarnings("unused")
    @ShadowSources({ "vehicle", "previousVisit.cumulativeDemand" })
    public Integer cumulativeDemandSupplier() {
        if (vehicle == null) {
            return null;
        }
        if (previousVisit == null) {
            return demand;
        }
        Integer previousCumulativeDemand = previousVisit.getCumulativeDemand();
        return previousCumulativeDemand == null ? null : previousCumulativeDemand + demand;
    }

    public OffsetDateTime getArrivalTime() {
        return timings == null ? null : timings.arrivalTime();
    }

    public OffsetDateTime getStartServiceTime() {
        return timings == null ? null : timings.startServiceTime();
    }

    public OffsetDateTime getDepartureTime() {
        return timings == null ? null : timings.departureTime();
    }

    public Long getCumulativeDrivingTimeSeconds() {
        return timings == null ? null : timings.cumulativeDrivingTimeSeconds();
    }

    public boolean isAssigned() {
        return vehicle != null;
    }

    public boolean isServiceFinishedAfterMaxEndTime() {
        var serviceStart = getStartServiceTime();
        return serviceStart != null
                && serviceStart.plus(serviceDuration).isAfter(maxEndTime);
    }

    public long getServiceFinishedDelayInMinutes() {
        var departureTime = getDepartureTime();
        if (departureTime == null) {
            return 0;
        }
        return roundDurationToNextOrEqualMinutes(Duration.between(maxEndTime, departureTime));
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
            return vehicle.getHomeLocation().getTravelTimeTo(location).seconds();
        }
        return previousVisit.getLocation().getTravelTimeTo(location).seconds();
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

    public Timings getTimings() {
        return timings;
    }

    public void setTimings(Timings timings) {
        this.timings = timings;
    }

    public Integer getCumulativeDemand() {
        return cumulativeDemand;
    }

    public void setCumulativeDemand(Integer cumulativeDemand) {
        this.cumulativeDemand = cumulativeDemand;
    }

    /**
     * The times at which this visit is serviced, all derived from the route this visit is in.
     *
     * @param arrivalTime the time the vehicle arrives, which may be before the visit is ready
     * @param startServiceTime the time servicing starts: the arrival time, or the visit's earliest
     *        start time when the vehicle has to wait
     * @param departureTime the time the vehicle leaves again
     * @param cumulativeDrivingTimeSeconds the driving time from the vehicle's home location up to
     *        this visit, not including the drive back home
     */
    public record Timings(OffsetDateTime arrivalTime, OffsetDateTime startServiceTime,
            OffsetDateTime departureTime, long cumulativeDrivingTimeSeconds) {
    }

}
