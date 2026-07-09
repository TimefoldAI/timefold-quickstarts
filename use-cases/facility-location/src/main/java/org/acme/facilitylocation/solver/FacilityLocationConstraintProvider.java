package org.acme.facilitylocation.solver;

import static ai.timefold.solver.core.api.score.HardMediumSoftScore.ONE_HARD;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;

public class FacilityLocationConstraintProvider implements ConstraintProvider {

    public static final String FACILITY_CAPACITY = "facility capacity";
    public static final String FACILITY_SETUP_COST = "facility setup cost";
    public static final String DISTANCE_FROM_FACILITY = "distance from facility";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] { facilityCapacity(constraintFactory), setupCost(constraintFactory),
                distanceFromFacility(constraintFactory) };
    }

    Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class).groupBy(Consumer::getFacility, sum(Consumer::getDemand))
                .filter((facility, demand) -> demand > facility.getCapacity())
                .penalize(ONE_HARD, (facility, demand) -> demand - facility.getCapacity())
                .asConstraint(new ConstraintInfo(FACILITY_CAPACITY, FACILITY_CAPACITY,
                        "A facility must not be assigned more demand than its capacity.",
                        FacilityLocationConstraintGroup.RESOURCE_LIMITED_PLANNING));
    }

    Constraint setupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class).groupBy(Consumer::getFacility)
                .penalize(HardMediumSoftScore.ofSoft(2), Facility::getSetupCost)
                .asConstraint(new ConstraintInfo(FACILITY_SETUP_COST, FACILITY_SETUP_COST,
                        "Penalize the setup cost of every facility that is used.",
                        FacilityLocationConstraintGroup.COST_MANAGEMENT));
    }

    Constraint distanceFromFacility(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class).filter(Consumer::isAssigned)
                .penalize(HardMediumSoftScore.ofSoft(5), Consumer::distanceFromFacility)
                .asConstraint(new ConstraintInfo(DISTANCE_FROM_FACILITY, DISTANCE_FROM_FACILITY,
                        "Reduce the distance between each consumer and its assigned facility.",
                        FacilityLocationConstraintGroup.TRAVEL_AND_DISTANCE));
    }
}
