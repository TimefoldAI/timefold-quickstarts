package org.acme.orderpicking.solver;

public enum ConstraintGroupTag {
    BUCKET_CAPACITY("bucket capacity"),
    TRAVEL_EFFICIENCY("travel efficiency"),
    ORDER_INTEGRITY("order integrity");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
