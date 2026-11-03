package org.acme.tournamentschedule.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class TournamentScheduleConstraintGroup {

    public static final ConstraintGroupInfo SCHEDULING_CONFLICTS = new ConstraintGroupInfo("schedulingConflicts",
            "Scheduling conflicts",
            "Avoid assigning a team twice on the same day, and respect each team's unavailable days.",
            "IconCalendarOff",
            new String[] { "scheduling conflicts" });

    public static final ConstraintGroupInfo FAIRNESS = new ConstraintGroupInfo("fairness",
            "Fairness",
            "Spread the number of matches evenly across all teams.",
            "IconScale",
            new String[] { "fairness" });

    public static final ConstraintGroupInfo MATCH_BALANCE = new ConstraintGroupInfo("matchBalance",
            "Match balance",
            "Balance how often each pair of teams faces each other.",
            "IconArrowsShuffle",
            new String[] { "match balance" });

    private TournamentScheduleConstraintGroup() {
    }
}
