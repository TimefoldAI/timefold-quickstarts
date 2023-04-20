package org.acme.facilitylocation.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sumLong;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityLocationConstraintConfiguration;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class FacilityLocationConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                facilityCapacity(constraintFactory),
                setupCost(constraintFactory),
                distanceFromFacility(constraintFactory)
        };
    }

    Constraint facilityCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility, sumLong(Consumer::getDemand))
                .filter((facility, demand) -> demand > facility.getCapacity())
                .penalizeConfigurableLong((facility, demand) -> demand - facility.getCapacity())
                .asConstraint(FacilityLocationConstraintConfiguration.FACILITY_CAPACITY);
    }

    Constraint setupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .groupBy(Consumer::getFacility)
                .penalizeConfigurableLong(Facility::getSetupCost)
                .asConstraint(FacilityLocationConstraintConfiguration.FACILITY_SETUP_COST);
    }

    Constraint distanceFromFacility(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Consumer.class)
                .filter(Consumer::isAssigned)
                .penalizeConfigurableLong(Consumer::distanceFromFacility)
                .asConstraint(FacilityLocationConstraintConfiguration.DISTANCE_FROM_FACILITY);
    }
}
