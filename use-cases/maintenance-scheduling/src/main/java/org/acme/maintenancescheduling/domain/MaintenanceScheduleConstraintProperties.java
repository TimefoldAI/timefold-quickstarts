package org.acme.maintenancescheduling.domain;

public final class MaintenanceScheduleConstraintProperties {

    public static final String CREW_CONFLICT = "Crew conflict";
    public static final String MIN_START_DATE = "Min start date";
    public static final String MAX_END_DATE = "Max end date";

    public static final String BEFORE_IDEAL_END_DATE = "Before ideal end date";
    public static final String AFTER_IDEAL_END_DATE = "After ideal end date";
    public static final String TAG_CONFLICT = "Tag conflict";

    private MaintenanceScheduleConstraintProperties() {
    }
}
