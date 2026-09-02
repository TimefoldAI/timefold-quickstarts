package org.acme.vehiclerouting.domain;

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
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInputMetrics;
import org.acme.vehiclerouting.dto.output.VehicleRoutePlanOutputMetrics;

/**
 * The plan for routing vehicles to visits, including:
 * <ul>
 * <li>capacity - each vehicle has a capacity for visits demand,</li>
 * <li>time windows - each visit accepts the vehicle only in specified time window.</li>
 * </ul>
 *
 * The planning solution is optimized according to the driving time (as opposed to the travel distance, for example)
 * because it is easy to determine if the vehicle arrival time fits into the visit time window.
 * In addition, optimizing travel time optimizes the distance too, as a side effect - in case there is a faster route,
 * the travel time takes precedence (highway vs. local road).
 */
@PlanningSolution
public class VehicleRoutePlan implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<VehicleRoutePlanInputMetrics>, OutputMetricsAware<VehicleRoutePlanOutputMetrics> {

    @PlanningEntityCollectionProperty
    private List<Vehicle> vehicles;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Visit> visits;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public VehicleRoutePlan() {
    }

    /**
     * Also computes the driving time matrix between every location in the plan, since a vehicle's
     * route length is only defined once every pair of its locations has a driving time.
     */
    public VehicleRoutePlan(List<Vehicle> vehicles, List<Visit> visits) {
        this.vehicles = vehicles;
        this.visits = visits;
        List<Location> locations = Stream.concat(
                vehicles.stream().map(Vehicle::getHomeLocation),
                visits.stream().map(Visit::getLocation)).toList();

        DrivingTimeCalculator drivingTimeCalculator = HaversineDrivingTimeCalculator.getInstance();
        drivingTimeCalculator.initDrivingTimeMaps(locations);
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public long getTotalDrivingTimeSeconds() {
        return vehicles == null ? 0 : vehicles.stream().mapToLong(Vehicle::getTotalDrivingTimeSeconds).sum();
    }

    @Override
    public VehicleRoutePlanInputMetrics getInputMetrics() {
        int totalDemand = visits.stream().mapToInt(Visit::getDemand).sum();
        int totalCapacity = vehicles.stream().mapToInt(Vehicle::getCapacity).sum();
        return new VehicleRoutePlanInputMetrics(visits.size(), vehicles.size(), totalDemand, totalCapacity);
    }

    @Override
    public VehicleRoutePlanOutputMetrics getOutputMetrics() {
        // Read the list variable rather than Visit.vehicle: the inverse relation shadow is not
        // computed yet on a solution that has only been loaded, but the route lists always are.
        int assignedVisits = vehicles.stream().mapToInt(vehicle -> vehicle.getVisits().size()).sum();
        int unassignedVisits = visits.size() - assignedVisits;
        int usedVehicles = (int) vehicles.stream().filter(vehicle -> !vehicle.getVisits().isEmpty()).count();
        return new VehicleRoutePlanOutputMetrics(assignedVisits, unassignedVisits, usedVehicles,
                getTotalDrivingTimeSeconds());
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<Visit> getVisits() {
        return visits;
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

}
