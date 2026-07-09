package org.acme.vehiclerouting.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.vehiclerouting.domain.geo.DrivingTimeCalculator;
import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator;
import org.acme.vehiclerouting.dto.VehicleRoutingInputMetrics;
import org.acme.vehiclerouting.dto.VehicleRoutingOutputMetrics;

/**
 * The plan for routing vehicles to visits, including:
 * <ul>
 * <li>capacity - each vehicle has a capacity for visits demand,</li>
 * <li>time windows - each visit accepts the vehicle only in specified time window.</li>
 * </ul>
 *
 * The planning solution is optimized according to the driving time (as opposed to the travel distance, for example)
 * because it is easy to determine if the vehicle arrival time fits into the visit time window.
 */
@PlanningSolution
public class VehicleRoutePlan implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<VehicleRoutingInputMetrics>, OutputMetricsAware<VehicleRoutingOutputMetrics> {

    private String name;

    private Location southWestCorner;
    private Location northEastCorner;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @PlanningEntityCollectionProperty
    private List<Vehicle> vehicles;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Visit> visits;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public VehicleRoutePlan() {
        // Marshalling constructor
    }

    public VehicleRoutePlan(String name, Location southWestCorner, Location northEastCorner,
            LocalDateTime startDateTime, LocalDateTime endDateTime, List<Vehicle> vehicles, List<Visit> visits) {
        this.name = name;
        this.southWestCorner = southWestCorner;
        this.northEastCorner = northEastCorner;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.vehicles = vehicles;
        this.visits = visits;
        List<Location> locations = Stream.concat(
                vehicles.stream().map(Vehicle::getHomeLocation),
                visits.stream().map(Visit::getLocation)).toList();

        DrivingTimeCalculator drivingTimeCalculator = HaversineDrivingTimeCalculator.getInstance();
        drivingTimeCalculator.initDrivingTimeMaps(locations);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getSouthWestCorner() {
        return southWestCorner;
    }

    public void setSouthWestCorner(Location southWestCorner) {
        this.southWestCorner = southWestCorner;
    }

    public Location getNorthEastCorner() {
        return northEastCorner;
    }

    public void setNorthEastCorner(Location northEastCorner) {
        this.northEastCorner = northEastCorner;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    @Override
    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public long getTotalDrivingTimeSeconds() {
        return vehicles == null ? 0 : vehicles.stream().mapToLong(Vehicle::getTotalDrivingTimeSeconds).sum();
    }

    @Override
    public VehicleRoutingInputMetrics getInputMetrics() {
        return new VehicleRoutingInputMetrics(vehicles.size(), visits.size());
    }

    @Override
    public VehicleRoutingOutputMetrics getOutputMetrics() {
        int assignedVisits = (int) visits.stream().filter(visit -> visit.getVehicle() != null).count();
        int unassignedVisits = visits.size() - assignedVisits;
        int usedVehicles = (int) vehicles.stream().filter(vehicle -> !vehicle.getVisits().isEmpty()).count();
        return new VehicleRoutingOutputMetrics(assignedVisits, unassignedVisits, usedVehicles, getTotalDrivingTimeSeconds());
    }

    @Override
    public String toString() {
        return "VehicleRoutePlan{visits: " + visits.size() + ", score: " + score + '}';
    }
}
