package org.acme.maintenancescheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class MaintenanceScheduleConstraintGroup {

    public static final ConstraintGroupInfo CREW_CONFLICTS = new ConstraintGroupInfo("crewConflicts",
            "Crew conflicts",
            "Avoid assigning one crew to two maintenance jobs that are worked on at the same time.",
            "IconUsers",
            new String[] { "crew conflicts" });

    public static final ConstraintGroupInfo MAINTENANCE_WINDOW = new ConstraintGroupInfo("maintenanceWindow",
            "Maintenance window",
            "Keep every job inside the window between the date it is ready to start and the date it is due.",
            "IconCalendar",
            new String[] { "maintenance window" });

    public static final ConstraintGroupInfo MAINTENANCE_TIMING = new ConstraintGroupInfo("maintenanceTiming",
            "Maintenance timing",
            "Finish every job as close as possible to its ideal end date, so maintenance is neither repeated too "
                    + "soon nor at risk of running over its due date.",
            "IconClock",
            new String[] { "maintenance timing" });

    public static final ConstraintGroupInfo TAG_CONFLICTS = new ConstraintGroupInfo("tagConflicts",
            "Tag conflicts",
            "Avoid working on jobs that share a tag at the same time, for example road works in the same area.",
            "IconTags",
            new String[] { "tag conflicts" });

    private MaintenanceScheduleConstraintGroup() {
    }
}
