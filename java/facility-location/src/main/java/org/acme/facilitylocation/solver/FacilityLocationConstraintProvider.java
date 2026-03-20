package org.acme.facilitylocation.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;

import static ai.timefold.solver.core.api.score.HardSoftScore.ONE_HARD;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

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

    Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility, sum(Consumer::getDemand))
                .filter((facility, demand) -> demand > facility.getCapacity())
                .penalize(ONE_HARD, (facility, demand) -> demand - facility.getCapacity())
                .asConstraint("facility capacity");
    }

    Constraint setupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility)
                .penalize(HardSoftScore.ofSoft(2), Facility::getSetupCost)
                .asConstraint("facility setup cost");
    }

    Constraint distanceFromFacility(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .filter(Consumer::isAssigned)
                .penalize(HardSoftScore.ofSoft(5), Consumer::distanceFromFacility)
                .asConstraint("distance from facility");
    }
}
