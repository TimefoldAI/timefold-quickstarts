package org.acme.maintenancescheduling.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    DEADLINE_COMPLIANCE("deadline compliance"),
    MAINTENANCE_PREFERENCES("maintenance preferences");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
