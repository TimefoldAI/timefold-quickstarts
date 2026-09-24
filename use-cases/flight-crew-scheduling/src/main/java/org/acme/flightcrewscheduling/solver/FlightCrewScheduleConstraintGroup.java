package org.acme.flightcrewscheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class FlightCrewScheduleConstraintGroup {

    public static final ConstraintGroupInfo CREW_QUALIFICATION = new ConstraintGroupInfo("crewQualification",
            "Crew qualification",
            "Only assign a crew seat to someone who holds the skill that seat requires.",
            "IconCertificate",
            new String[] { "crew qualification" });

    public static final ConstraintGroupInfo CREW_AVAILABILITY = new ConstraintGroupInfo("crewAvailability",
            "Crew availability",
            "Keep a crew member off overlapping flights and off flights that run on a day they are unavailable.",
            "IconCalendarTime",
            new String[] { "crew availability" });

    public static final ConstraintGroupInfo ROUTE_CONTINUITY = new ConstraintGroupInfo("routeContinuity",
            "Route continuity",
            "Only chain flights that depart from the airport where the crew member's previous flight landed.",
            "IconRoute",
            new String[] { "route continuity" });

    public static final ConstraintGroupInfo HOME_BASE = new ConstraintGroupInfo("homeBase",
            "Home base",
            "Start and end each crew member's roster at their own home airport.",
            "IconHome",
            new String[] { "home base" });

    private FlightCrewScheduleConstraintGroup() {
    }
}
