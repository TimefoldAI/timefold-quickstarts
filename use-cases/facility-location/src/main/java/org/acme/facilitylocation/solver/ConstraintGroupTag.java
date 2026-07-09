package org.acme.facilitylocation.solver;

public enum ConstraintGroupTag {
    SERVICE_QUALITY("service quality"),
    FINANCIAL_GAINS("financial gains"),
    ENVIRONMENTAL_GAINS("environmental gains");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
