package org.acme.flightcrewscheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class FlightCrewSchedulingConstraintGroup {
    public static final ConstraintGroupInfo CREW_FEASIBILITY = new ConstraintGroupInfo("crewFeasibility",
            "Crew feasibility",
            "Assign qualified, available crew without overlapping or impossible flight transfers.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo HOME_BASE_PREFERENCES = new ConstraintGroupInfo("homeBasePreferences",
            "Home base preferences",
            "Start and end each employee's sequence of flights at their home airport.",
            "IconUser",
            new String[] { ConstraintGroupTag.CREW_SATISFACTION.getTag() });

    private FlightCrewSchedulingConstraintGroup() {
    }
}
