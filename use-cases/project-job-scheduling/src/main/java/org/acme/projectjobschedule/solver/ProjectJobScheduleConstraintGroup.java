package org.acme.projectjobschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class ProjectJobScheduleConstraintGroup {
    public static final ConstraintGroupInfo RESOURCE_CAPACITY = new ConstraintGroupInfo("resourceCapacity",
            "Resource capacity",
            "Ensure no resource is used beyond its available capacity.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.RESOURCE_FEASIBILITY.getTag() });
    public static final ConstraintGroupInfo PROJECT_DELAY = new ConstraintGroupInfo("projectDelay",
            "Project delay",
            "Finish each project as close as possible to its critical path end date.",
            "IconClock",
            new String[] { ConstraintGroupTag.ON_TIME_DELIVERY.getTag() });
    public static final ConstraintGroupInfo MAKESPAN = new ConstraintGroupInfo("makespan",
            "Makespan",
            "Keep the overall schedule as short as possible.",
            "IconBook",
            new String[] { ConstraintGroupTag.SHORT_SCHEDULE.getTag() });

    private ProjectJobScheduleConstraintGroup() {
    }
}
