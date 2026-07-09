package org.acme.flightcrewscheduling.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    CREW_SATISFACTION("crew satisfaction");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
