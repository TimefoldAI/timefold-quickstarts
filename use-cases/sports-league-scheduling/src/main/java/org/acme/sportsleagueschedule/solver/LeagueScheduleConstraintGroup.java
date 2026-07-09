package org.acme.sportsleagueschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class LeagueScheduleConstraintGroup {

    public static final ConstraintGroupInfo SCHEDULE_FEASIBILITY = new ConstraintGroupInfo("scheduleFeasibility",
            "Schedule feasibility",
            "Avoid scheduling conflicts and undesirable match sequences for teams.",
            "IconCalendar",
            new String[] { ConstraintGroupTag.SCHEDULE_FEASIBILITY.getTag() });

    public static final ConstraintGroupInfo TRAVEL_DISTANCE = new ConstraintGroupInfo("travelDistance",
            "Travel distance",
            "Minimize the distance teams travel between consecutive matches.",
            "IconRoute",
            new String[] { ConstraintGroupTag.TRAVEL_DISTANCE.getTag() });

    public static final ConstraintGroupInfo MATCH_IMPORTANCE = new ConstraintGroupInfo("matchImportance",
            "Match importance",
            "Schedule classic matches on weekends or holidays.",
            "IconStar",
            new String[] { ConstraintGroupTag.MATCH_IMPORTANCE.getTag() });

    private LeagueScheduleConstraintGroup() {
    }
}
