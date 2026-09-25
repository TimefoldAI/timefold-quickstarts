package org.acme.facilitylocation.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;
import ai.timefold.solver.service.maps.api.model.Location;
import ai.timefold.solver.service.maps.service.integration.api.LocationsAwareSolverModel;

import org.acme.facilitylocation.dto.input.FacilityPlanInputMetrics;
import org.acme.facilitylocation.dto.output.FacilityPlanOutputMetrics;

@PlanningSolution
public class FacilityPlan implements LocationsAwareSolverModel<HardSoftScore>,
        InputMetricsAware<FacilityPlanInputMetrics>, OutputMetricsAware<FacilityPlanOutputMetrics> {

    // Facilities are shadow planning entities (they carry the inverse relation of Consumer.facility) and at the
    // same time the value range of that very variable.
    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Facility> facilities;
    @PlanningEntityCollectionProperty
    private List<Consumer> consumers;

    @PlanningScore
    private HardSoftScore score;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    // Reported back by the map-service through setLocationsNotInMap() after it builds the distance
    // matrix returned by getLocations(): the locations it could not resolve, if any.
    private List<Location> locationsNotInMap = List.of();

    public FacilityPlan() {
    }

    public FacilityPlan(List<Facility> facilities, List<Consumer> consumers) {
        this.facilities = facilities;
        this.consumers = consumers;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public FacilityPlanInputMetrics getInputMetrics() {
        long totalCapacity = facilities.stream().mapToLong(Facility::getCapacity).sum();
        long totalDemand = consumers.stream().mapToLong(Consumer::getDemand).sum();
        return new FacilityPlanInputMetrics(facilities.size(), consumers.size(), totalCapacity, totalDemand);
    }

    @Override
    public FacilityPlanOutputMetrics getOutputMetrics() {
        // Derived from the genuine variable (Consumer.facility) rather than from Facility's inverse relation, so
        // these numbers are correct even for a solver model whose shadow variables were never initialized.
        int assignedConsumers = (int) consumers.stream().map(Consumer::getFacility).filter(Objects::nonNull).count();
        List<Facility> usedFacilities = consumers.stream()
                .map(Consumer::getFacility)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        long totalSetupCost = usedFacilities.stream().mapToLong(Facility::getSetupCost).sum();
        long totalDistance = consumers.stream()
                .filter(consumer -> consumer.getFacility() != null)
                .mapToLong(Consumer::distanceFromFacility)
                .sum();
        return new FacilityPlanOutputMetrics(assignedConsumers, consumers.size() - assignedConsumers,
                usedFacilities.size(), totalSetupCost, totalDistance);
    }

    // ── LocationsAwareSolverModel ──
    // The map-service uses these to build the distance matrix that Location.getDistanceTo()
    // relies on, before the solver runs.
    @Override
    public List<Location> getLocations() {
        if (facilities == null || consumers == null) {
            return List.of();
        }
        return Stream.concat(
                facilities.stream().map(Facility::getLocation),
                consumers.stream().map(Consumer::getLocation)).toList();
    }

    // Every solve builds its own one-off matrix rather than reusing a named, pre-built one.
    @Override
    public Optional<String> getLocationSetName() {
        return Optional.empty();
    }

    @Override
    public void setLocationsNotInMap(List<Location> locationsNotInMap) {
        this.locationsNotInMap = locationsNotInMap == null ? List.of() : locationsNotInMap;
    }

    @Override
    public List<Location> getLocationsNotInMap() {
        return locationsNotInMap;
    }

    @Override
    public String toString() {
        return "FacilityPlan{" +
                "facilities: " + facilities.size() +
                ", consumers: " + consumers.size() +
                ", score: " + score +
                '}';
    }
}
