package org.acme.projectjobschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class ProjectJobScheduleConstraintGroup {

    public static final ConstraintGroupInfo RESOURCE_CAPACITY = new ConstraintGroupInfo("resourceCapacity",
            "Resource capacity",
            "Never consume more of a resource than it has: per day for a renewable resource, over the whole "
                    + "schedule for a non-renewable one.",
            "IconStack2",
            new String[] { "resource capacity" });

    public static final ConstraintGroupInfo PROJECT_DELAY = new ConstraintGroupInfo("projectDelay",
            "Project delay",
            "Finish each project as close to its critical path end date as possible.",
            "IconClockExclamation",
            new String[] { "project delay" });

    public static final ConstraintGroupInfo MAKESPAN = new ConstraintGroupInfo("makespan",
            "Makespan",
            "Keep the overall schedule short, so the last project ends as early as possible.",
            "IconTimeline",
            new String[] { "makespan" });

    private ProjectJobScheduleConstraintGroup() {
    }
}
