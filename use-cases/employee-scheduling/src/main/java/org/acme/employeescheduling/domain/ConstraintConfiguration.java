package org.acme.employeescheduling.domain;

/**
 * Configuration for configurable constraints. Treated as a ProblemFact so it can be set via the API.
 */
public class ConstraintConfiguration {

    public enum Severity { NONE, SOFT, HARD }

    private Severity mustWorkTogetherSeverity = Severity.HARD;
    private Severity maxWeeklySeverity = Severity.HARD;
    private Severity maxMonthlySeverity = Severity.HARD;

    public ConstraintConfiguration() {
    }

    public Severity getMustWorkTogetherSeverity() {
        return mustWorkTogetherSeverity;
    }

    public void setMustWorkTogetherSeverity(Severity mustWorkTogetherSeverity) {
        this.mustWorkTogetherSeverity = mustWorkTogetherSeverity;
    }

    public Severity getMaxWeeklySeverity() {
        return maxWeeklySeverity;
    }

    public void setMaxWeeklySeverity(Severity maxWeeklySeverity) {
        this.maxWeeklySeverity = maxWeeklySeverity;
    }

    public Severity getMaxMonthlySeverity() {
        return maxMonthlySeverity;
    }

    public void setMaxMonthlySeverity(Severity maxMonthlySeverity) {
        this.maxMonthlySeverity = maxMonthlySeverity;
    }
}
