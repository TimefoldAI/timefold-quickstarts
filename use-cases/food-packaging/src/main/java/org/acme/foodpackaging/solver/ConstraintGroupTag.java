package org.acme.foodpackaging.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    ON_TIME_DELIVERY("on-time delivery"),
    PRODUCTION_EFFICIENCY("production efficiency");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
