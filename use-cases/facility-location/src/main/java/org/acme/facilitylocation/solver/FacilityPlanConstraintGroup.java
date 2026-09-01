package org.acme.facilitylocation.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class FacilityPlanConstraintGroup {

    public static final ConstraintGroupInfo FACILITY_CAPACITY = new ConstraintGroupInfo("facilityCapacity",
            "Facility capacity",
            "Never assign more consumer demand to a facility than that facility can serve.",
            "IconBuildingWarehouse",
            new String[] { "capacity" });

    public static final ConstraintGroupInfo SETUP_COST = new ConstraintGroupInfo("setupCost",
            "Setup cost",
            "Keep the total setup cost down by using as few and as cheap facilities as possible.",
            "IconCoin",
            new String[] { "cost" });

    public static final ConstraintGroupInfo PROXIMITY = new ConstraintGroupInfo("proximity",
            "Proximity",
            "Serve every consumer from a facility that is as close to it as possible.",
            "IconMapPin",
            new String[] { "proximity" });

    private FacilityPlanConstraintGroup() {
    }
}
