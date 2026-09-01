package org.acme.facilitylocation.solver;

import static ai.timefold.solver.core.api.score.HardSoftScore.ONE_HARD;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.FacilityPlanConstraintProperties;
import org.acme.facilitylocation.domain.justification.FacilityPlanJustification.DistanceFromFacilityJustification;
import org.acme.facilitylocation.domain.justification.FacilityPlanJustification.FacilityCapacityJustification;
import org.acme.facilitylocation.domain.justification.FacilityPlanJustification.FacilitySetupCostJustification;

public class FacilityLocationConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                facilityCapacity(constraintFactory),

                // Soft constraints
                setupCost(constraintFactory),
                distanceFromFacility(constraintFactory)
        };
    }

    public Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility, sum(Consumer::getDemand))
                .filter((facility, demand) -> demand > facility.getCapacity())
                .penalize(ONE_HARD, (facility, demand) -> demand - facility.getCapacity())
                .justifyWith((facility, demand, score) -> FacilityCapacityJustification.of(facility, demand))
                .asConstraint(new ConstraintInfo(FacilityPlanConstraintProperties.FACILITY_CAPACITY,
                        FacilityPlanConstraintProperties.FACILITY_CAPACITY,
                        "The total demand of the consumers served by a facility must not exceed that facility's capacity.",
                        FacilityPlanConstraintGroup.FACILITY_CAPACITY));
    }

    public Constraint setupCost(ConstraintFactory constraintFactory) {
        // Grouping by facility yields one match per facility that serves at least one consumer, which is exactly
        // the set of facilities whose setup cost has to be paid. The consumer count is only carried along to
        // make the justification more informative.
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility, count())
                .penalize(HardSoftScore.ofSoft(2), (facility, consumerCount) -> facility.getSetupCost())
                .justifyWith((facility, consumerCount, score) -> FacilitySetupCostJustification.of(facility,
                        consumerCount.intValue()))
                .asConstraint(new ConstraintInfo(FacilityPlanConstraintProperties.FACILITY_SETUP_COST,
                        FacilityPlanConstraintProperties.FACILITY_SETUP_COST,
                        "Minimize the total setup cost of the facilities that serve at least one consumer.",
                        FacilityPlanConstraintGroup.SETUP_COST));
    }

    public Constraint distanceFromFacility(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .penalize(HardSoftScore.ofSoft(5), Consumer::distanceFromFacility)
                .justifyWith((consumer, score) -> DistanceFromFacilityJustification.of(consumer))
                .asConstraint(new ConstraintInfo(FacilityPlanConstraintProperties.DISTANCE_FROM_FACILITY,
                        FacilityPlanConstraintProperties.DISTANCE_FROM_FACILITY,
                        "Minimize the total distance, in meters, between the consumers and the facility serving them.",
                        FacilityPlanConstraintGroup.PROXIMITY));
    }
}
