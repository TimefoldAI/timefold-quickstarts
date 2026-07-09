package org.acme.bedallocation.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    PATIENT_SAFETY("patient safety"),
    PATIENT_COMFORT("patient comfort");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
