package org.acme.vehiclerouting.solver;

public enum ConstraintGroupTag {
    CAPACITY("capacity"),
    TIME_WINDOWS("time windows"),
    VISIT_ASSIGNMENT("visit assignment"),
    TRAVEL_TIME("travel time");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
