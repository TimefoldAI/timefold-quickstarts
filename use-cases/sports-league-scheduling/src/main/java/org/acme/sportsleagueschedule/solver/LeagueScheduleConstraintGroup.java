package org.acme.sportsleagueschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class LeagueScheduleConstraintGroup {

    public static final ConstraintGroupInfo SCHEDULE_CONFLICTS = new ConstraintGroupInfo("scheduleConflicts",
            "Schedule conflicts",
            "Avoid scheduling a team twice on the same matchday, or replaying the same pairing right after it "
                    + "was played.",
            "IconCalendarX",
            new String[] { "schedule conflicts" });

    public static final ConstraintGroupInfo TEAM_FAIRNESS = new ConstraintGroupInfo("teamFairness",
            "Team fairness",
            "Alternate home and away matches, so no team plays four or more matchdays in a row at home or away.",
            "IconScale",
            new String[] { "team fairness" });

    public static final ConstraintGroupInfo TRAVEL_DISTANCE = new ConstraintGroupInfo("travelDistance",
            "Travel distance",
            "Keep the distance every team travels between the venues of its consecutive matches as short as possible.",
            "IconRoute",
            new String[] { "travel distance" });

    public static final ConstraintGroupInfo MATCH_ATTRACTIVENESS = new ConstraintGroupInfo("matchAttractiveness",
            "Match attractiveness",
            "Schedule classic matches, such as derbies, on a weekend or holiday round, when they draw the "
                    + "biggest crowd.",
            "IconStar",
            new String[] { "match attractiveness" });

    private LeagueScheduleConstraintGroup() {
    }
}
