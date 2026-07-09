package org.acme.facilitylocation.domain;

import java.util.Arrays;
import java.util.List;

import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;

import org.acme.facilitylocation.dto.FacilityLocationInputMetrics;
import org.acme.facilitylocation.dto.FacilityLocationOutputMetrics;

@PlanningSolution
public class FacilityLocationProblem implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<FacilityLocationInputMetrics>, OutputMetricsAware<FacilityLocationOutputMetrics> {

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Facility> facilities;
    @PlanningEntityCollectionProperty
    private List<Consumer> consumers;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides
            .none();

    private Location southWestCorner;
    private Location northEastCorner;

    public FacilityLocationProblem() {
    }

    public FacilityLocationProblem(List<Facility> facilities, List<Consumer> consumers, Location southWestCorner,
            Location northEastCorner) {
        this.facilities = facilities;
        this.consumers = consumers;
        this.southWestCorner = southWestCorner;
        this.northEastCorner = northEastCorner;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<Facility> facilities) {
        this.facilities = facilities;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
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

    public void setConstraintWeightOverrides(
            ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    public List<Location> getBounds() {
        return Arrays.asList(southWestCorner, northEastCorner);
    }

    public long getTotalCost() {
        return facilities.stream().filter(Facility::isUsed).mapToLong(Facility::getSetupCost).sum();
    }

    public long getPotentialCost() {
        return facilities.stream().mapToLong(Facility::getSetupCost).sum();
    }

    public String getTotalDistance() {
        long distance = consumers.stream().filter(Consumer::isAssigned).mapToLong(Consumer::distanceFromFacility).sum();
        return distance / 1000 + " km";
    }

    @Override
    public FacilityLocationInputMetrics getInputMetrics() {
        long totalDemand = consumers.stream().mapToLong(Consumer::getDemand).sum();
        long totalCapacity = facilities.stream().mapToLong(Facility::getCapacity).sum();
        return new FacilityLocationInputMetrics(facilities.size(), consumers.size(), totalDemand, totalCapacity,
                getPotentialCost());
    }

    @Override
    public FacilityLocationOutputMetrics getOutputMetrics() {
        int activatedFacilities = (int) facilities.stream().filter(Facility::isUsed).count();
        int assignedConsumers = (int) consumers.stream().filter(Consumer::isAssigned).count();
        int unassignedConsumers = consumers.size() - assignedConsumers;
        long totalTravelDistanceMeters =
                consumers.stream().filter(Consumer::isAssigned).mapToLong(Consumer::distanceFromFacility).sum();

        Long averageTravelDistanceMetersPerConsumer = assignedConsumers == 0
                ? null
                : Math.round((double) totalTravelDistanceMeters / assignedConsumers);

        long activatedCapacity = facilities.stream().filter(Facility::isUsed).mapToLong(Facility::getCapacity).sum();
        long activatedUsedCapacity =
                facilities.stream().filter(Facility::isUsed).mapToLong(Facility::getUsedCapacity).sum();
        Double capacityUtilizationPercentage = activatedCapacity == 0
                ? null
                : Math.round((double) activatedUsedCapacity / activatedCapacity * 10_000.0) / 100.0;

        int unusedFacilities = facilities.size() - activatedFacilities;
        return new FacilityLocationOutputMetrics(activatedFacilities, unusedFacilities, getTotalCost(),
                assignedConsumers, unassignedConsumers, totalTravelDistanceMeters,
                averageTravelDistanceMetersPerConsumer, capacityUtilizationPercentage);
    }

    @Override
    public String toString() {
        return "FacilityLocationProblem{" + "facilities: " + facilities.size() + ", consumers: " + consumers.size()
                + ", score: " + score + '}';
    }
}
