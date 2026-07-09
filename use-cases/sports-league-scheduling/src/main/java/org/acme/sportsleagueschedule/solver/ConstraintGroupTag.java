package org.acme.sportsleagueschedule.solver;

public enum ConstraintGroupTag {
    SCHEDULE_FEASIBILITY("schedule feasibility"),
    TRAVEL_DISTANCE("travel distance"),
    MATCH_IMPORTANCE("match importance");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
