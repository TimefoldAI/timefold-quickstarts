package org.acme.vehiclerouting.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class VehicleRoutingConstraintGroup {
    public static final ConstraintGroupInfo CAPACITY = new ConstraintGroupInfo("capacity",
            "Capacity",
            "Ensure no vehicle carries more demand than its capacity.",
            "IconBox",
            new String[] { ConstraintGroupTag.CAPACITY.getTag() });
    public static final ConstraintGroupInfo TIME_WINDOWS = new ConstraintGroupInfo("timeWindows",
            "Time windows",
            "Ensure every visit is serviced within its time window.",
            "IconClock",
            new String[] { ConstraintGroupTag.TIME_WINDOWS.getTag() });
    public static final ConstraintGroupInfo VISIT_ASSIGNMENT = new ConstraintGroupInfo("visitAssignment",
            "Visit assignment",
            "Assign as many visits as possible to vehicles.",
            "IconChecklist",
            new String[] { ConstraintGroupTag.VISIT_ASSIGNMENT.getTag() });
    public static final ConstraintGroupInfo TRAVEL_TIME = new ConstraintGroupInfo("travelTime",
            "Travel time",
            "Minimize the total driving time across all vehicles.",
            "IconRoute",
            new String[] { ConstraintGroupTag.TRAVEL_TIME.getTag() });

    private VehicleRoutingConstraintGroup() {
    }
}
