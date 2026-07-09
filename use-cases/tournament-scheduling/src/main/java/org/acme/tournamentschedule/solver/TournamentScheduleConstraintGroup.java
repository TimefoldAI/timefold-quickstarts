package org.acme.tournamentschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class TournamentScheduleConstraintGroup {
    public static final ConstraintGroupInfo CONFLICT_AVOIDANCE = new ConstraintGroupInfo("conflictAvoidance",
            "Conflict avoidance",
            "Ensure a team plays at most once per day.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo AVAILABILITY = new ConstraintGroupInfo("availability",
            "Availability",
            "Do not assign a team on a day on which it is unavailable.",
            "IconUser",
            new String[] { ConstraintGroupTag.TEAM_AVAILABILITY.getTag() });
    public static final ConstraintGroupInfo FAIRNESS = new ConstraintGroupInfo("fairness",
            "Fairness",
            "Balance the number of assignments per team and the confrontations between teams.",
            "IconBook",
            new String[] { ConstraintGroupTag.FAIRNESS.getTag() });

    private TournamentScheduleConstraintGroup() {
    }
}
