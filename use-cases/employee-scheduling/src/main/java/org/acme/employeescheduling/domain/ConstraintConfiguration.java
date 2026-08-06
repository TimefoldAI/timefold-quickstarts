package org.acme.employeescheduling.domain;

/**
 * Configuration for configurable constraints. Treated as a ProblemFact so it can be set via the API.
 */
public class ConstraintConfiguration {

    public enum Severity { NONE, SOFT, HARD }

    private Severity mustWorkTogetherSeverity = Severity.HARD;
    private Severity maxWeeklySeverity = Severity.HARD;
    private Severity maxMonthlySeverity = Severity.HARD;

    /**
     * Goal (target) number of shifts per employee per week. 0 disables the goal constraint.
     */
    private int targetShiftsPerWeek = 0;

    /**
     * Goal (target) number of minutes per employee per week. 0 disables the goal constraint.
     */
    private int targetMinutesPerWeek = 0;

    /**
     * Severity for the target shifts per week goal: NONE, SOFT, or HARD.
     */
    private Severity targetShiftsPerWeekSeverity = Severity.SOFT;

    /**
     * Severity for the target minutes per week goal: NONE, SOFT, or HARD.
     */
    private Severity targetMinutesPerWeekSeverity = Severity.SOFT;

    /**
     * Numeric limit for max weekly minutes. Defaults to 40 hours (in minutes).
     */
    private int maxWeeklyMinutes = 40 * 60;

    /**
     * Numeric limit for max monthly minutes. Defaults to 160 hours (in minutes).
     */
    private int maxMonthlyMinutes = 160 * 60;

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

    public int getTargetShiftsPerWeek() {
        return targetShiftsPerWeek;
    }

    public void setTargetShiftsPerWeek(int targetShiftsPerWeek) {
        this.targetShiftsPerWeek = targetShiftsPerWeek;
    }

    public int getTargetMinutesPerWeek() {
        return targetMinutesPerWeek;
    }

    public void setTargetMinutesPerWeek(int targetMinutesPerWeek) {
        this.targetMinutesPerWeek = targetMinutesPerWeek;
    }

    public Severity getTargetShiftsPerWeekSeverity() {
        return targetShiftsPerWeekSeverity;
    }

    public void setTargetShiftsPerWeekSeverity(Severity targetShiftsPerWeekSeverity) {
        this.targetShiftsPerWeekSeverity = targetShiftsPerWeekSeverity;
    }

    public Severity getTargetMinutesPerWeekSeverity() {
        return targetMinutesPerWeekSeverity;
    }

    public void setTargetMinutesPerWeekSeverity(Severity targetMinutesPerWeekSeverity) {
        this.targetMinutesPerWeekSeverity = targetMinutesPerWeekSeverity;
    }

    public int getMaxWeeklyMinutes() {
        return maxWeeklyMinutes;
    }

    public void setMaxWeeklyMinutes(int maxWeeklyMinutes) {
        this.maxWeeklyMinutes = maxWeeklyMinutes;
    }

    public int getMaxMonthlyMinutes() {
        return maxMonthlyMinutes;
    }

    public void setMaxMonthlyMinutes(int maxMonthlyMinutes) {
        this.maxMonthlyMinutes = maxMonthlyMinutes;
    }
}
