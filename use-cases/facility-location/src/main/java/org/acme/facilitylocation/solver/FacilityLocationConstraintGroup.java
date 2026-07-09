package org.acme.facilitylocation.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class FacilityLocationConstraintGroup {
    public static final ConstraintGroupInfo RESOURCE_LIMITED_PLANNING =
            new ConstraintGroupInfo("resourceLimitedPlanning", "Resource-limited planning",
                    "Respect the capacity of each facility when assigning consumers.",
                    "IconDiamond",
                    new String[] { ConstraintGroupTag.SERVICE_QUALITY.getTag() });
    public static final ConstraintGroupInfo COST_MANAGEMENT = new ConstraintGroupInfo("costManagement", "Cost management",
            "Keep the cost of setting up and operating facilities within budget.",
            "IconPigMoney",
            new String[] { ConstraintGroupTag.FINANCIAL_GAINS.getTag() });
    public static final ConstraintGroupInfo TRAVEL_AND_DISTANCE = new ConstraintGroupInfo("travelAndDistance",
            "Travel and distance",
            "Reduce the distance between consumers and the facilities serving them.",
            "IconRoute",
            new String[] { ConstraintGroupTag.ENVIRONMENTAL_GAINS.getTag() });

    private FacilityLocationConstraintGroup() {
    }
}
