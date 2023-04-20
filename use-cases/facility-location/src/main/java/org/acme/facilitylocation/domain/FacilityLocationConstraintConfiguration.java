package org.acme.facilitylocation.domain;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfiguration;
import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@ConstraintConfiguration
public class FacilityLocationConstraintConfiguration {

    public static final String FACILITY_CAPACITY = "facility capacity";
    public static final String FACILITY_SETUP_COST = "facility setup cost";
    public static final String DISTANCE_FROM_FACILITY = "distance from facility";

    @ConstraintWeight(FACILITY_CAPACITY)
    HardSoftLongScore facilityCapacity = HardSoftLongScore.ofHard(1);
    @ConstraintWeight(FACILITY_SETUP_COST)
    HardSoftLongScore facilitySetupCost = HardSoftLongScore.ofSoft(2);
    @ConstraintWeight(DISTANCE_FROM_FACILITY)
    HardSoftLongScore distanceFromFacility = HardSoftLongScore.ofSoft(5);
}
