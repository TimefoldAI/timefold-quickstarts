package org.acme.maintenancescheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class MaintenanceScheduleConstraintGroup {
    public static final ConstraintGroupInfo CONFLICT_AVOIDANCE = new ConstraintGroupInfo("conflictAvoidance",
            "Conflict avoidance",
            "Ensure no crew is double-booked and overlapping jobs do not share tags.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo DEADLINES = new ConstraintGroupInfo("deadlines",
            "Deadlines",
            "Respect the earliest start date and latest end date of each job.",
            "IconCalendar",
            new String[] { ConstraintGroupTag.DEADLINE_COMPLIANCE.getTag() });
    public static final ConstraintGroupInfo PREFERENCES = new ConstraintGroupInfo("preferences",
            "Maintenance preferences",
            "Schedule jobs close to their ideal end date.",
            "IconUser",
            new String[] { ConstraintGroupTag.MAINTENANCE_PREFERENCES.getTag() });

    private MaintenanceScheduleConstraintGroup() {
    }
}
