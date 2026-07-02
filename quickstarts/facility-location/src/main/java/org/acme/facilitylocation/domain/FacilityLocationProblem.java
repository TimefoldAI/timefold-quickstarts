package org.acme.facilitylocation.domain;

import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;

@PlanningSolution
public class FacilityLocationProblem {

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Facility> facilities;
    @PlanningEntityCollectionProperty
    private List<Consumer> consumers;

    @PlanningScore
    private HardSoftScore score;

    // Demonstrates how to override the weights of constraints.
    ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.of(
            Map.of("distance from facility", HardSoftScore.ofSoft(6)));

    private Location southWestCorner;
    private Location northEastCorner;

    public FacilityLocationProblem() {
    }

    public FacilityLocationProblem(
            List<Facility> facilities,
            List<Consumer> consumers,
            Location southWestCorner,
            Location northEastCorner) {
        this.facilities = facilities;
        this.consumers = consumers;
        this.southWestCorner = southWestCorner;
        this.northEastCorner = northEastCorner;
    }

    public static FacilityLocationProblem empty() {
        FacilityLocationProblem problem = new FacilityLocationProblem(
                emptyList(),
                emptyList(),
                new Location(-90, -180),
                new Location(90, 180));
        problem.setScore(HardSoftScore.ZERO);
        return problem;
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

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    public List<Location> getBounds() {
        return Arrays.asList(southWestCorner, northEastCorner);
    }

    public long getTotalCost() {
        return facilities.stream()
                .filter(Facility::isUsed)
                .mapToLong(Facility::getSetupCost)
                .sum();
    }

    public long getPotentialCost() {
        return facilities.stream()
                .mapToLong(Facility::getSetupCost)
                .sum();
    }

    public String getTotalDistance() {
        long distance = consumers.stream()
                .filter(Consumer::isAssigned)
                .mapToLong(Consumer::distanceFromFacility)
                .sum();
        return distance / 1000 + " km";
    }

    @Override
    public String toString() {
        return "FacilityLocationProblem{" +
                "facilities: " + facilities.size() +
                ", consumers: " + consumers.size() +
                ", score: " + score +
                '}';
    }
}
