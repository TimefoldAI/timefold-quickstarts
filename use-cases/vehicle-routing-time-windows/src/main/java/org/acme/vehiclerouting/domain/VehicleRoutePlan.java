package org.acme.vehiclerouting.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.geo.DistanceCalculator;
import org.acme.vehiclerouting.domain.geo.HaversineDistanceCalculator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@PlanningSolution
public class VehicleRoutePlan {

    private String name;

    @ProblemFactCollectionProperty
    private List<Depot> depots;

    @PlanningEntityCollectionProperty
    private List<Vehicle> vehicles;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Customer> customers;

    @PlanningScore
    private HardSoftLongScore score;

    private Location southWestCorner;
    private Location northEastCorner;

    private SolverStatus solverStatus;

    private String scoreExplanation;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    public VehicleRoutePlan() {
    }

    public VehicleRoutePlan(String name, HardSoftLongScore score, SolverStatus solverStatus) {
        this.name = name;
        this.score = score;
        this.solverStatus = solverStatus;
    }

    @JsonCreator
    public VehicleRoutePlan(@JsonProperty("name") String name,
            @JsonProperty("depots") List<Depot> depots,
            @JsonProperty("vehicles") List<Vehicle> vehicles,
            @JsonProperty("customers") List<Customer> customers,
            @JsonProperty("southWestCorner") Location southWestCorner,
            @JsonProperty("northEastCorner") Location northEastCorner,
            @JsonProperty("startDateTime") LocalDateTime startDateTime,
            @JsonProperty("endDateTime") LocalDateTime endDateTime) {
        this.name = name;
        this.depots = depots;
        this.vehicles = vehicles;
        this.customers = customers;
        this.southWestCorner = southWestCorner;
        this.northEastCorner = northEastCorner;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        List<Location> locations = Stream.concat(
                depots.stream().map(Depot::getLocation),
                customers.stream().map(Customer::getLocation)).toList();

        DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();
        distanceCalculator.initDistanceMaps(locations);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Depot> getDepots() {
        return depots;
    }

    public void setDepots(List<Depot> depots) {
        this.depots = depots;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    public void setSouthWestCorner(Location southWestCorner) {
        this.southWestCorner = southWestCorner;
    }

    public void setNorthEastCorner(Location northEastCorner) {
        this.northEastCorner = northEastCorner;
    }

    public Location getSouthWestCorner() {
        return southWestCorner;
    }

    public Location getNorthEastCorner() {
        return northEastCorner;
    }

    public void setScoreExplanation(String scoreExplanation) {
        this.scoreExplanation = scoreExplanation;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public long getTotalDrivingTimeSeconds() {
        return score == null ? 0 : -score.softScore();
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getScoreExplanation() {
        return scoreExplanation;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
