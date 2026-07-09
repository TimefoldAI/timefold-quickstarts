package org.acme.projectjobschedule.solver;

public enum ConstraintGroupTag {
    RESOURCE_FEASIBILITY("resource feasibility"),
    ON_TIME_DELIVERY("on-time delivery"),
    SHORT_SCHEDULE("short schedule");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
