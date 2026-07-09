package org.acme.foodpackaging.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class FoodPackagingConstraintGroup {
    public static final ConstraintGroupInfo SCHEDULE_FEASIBILITY = new ConstraintGroupInfo("scheduleFeasibility",
            "Schedule feasibility",
            "Keep jobs within their deadline and avoid operators cleaning two lines at once.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo DELIVERY_PERFORMANCE = new ConstraintGroupInfo("deliveryPerformance",
            "Delivery performance",
            "Finish jobs before their ideal end time and assign as many jobs as possible.",
            "IconCalendar",
            new String[] { ConstraintGroupTag.ON_TIME_DELIVERY.getTag() });
    public static final ConstraintGroupInfo EFFICIENCY = new ConstraintGroupInfo("efficiency",
            "Production efficiency",
            "Minimise the total production span across all lines.",
            "IconBolt",
            new String[] { ConstraintGroupTag.PRODUCTION_EFFICIENCY.getTag() });

    private FoodPackagingConstraintGroup() {
    }
}
