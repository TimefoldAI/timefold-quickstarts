package org.acme.foodpackaging.domain;

public final class PackagingScheduleConstraintProperties {

    public static final String MAX_END_DATE_TIME = "Max end date time";
    public static final String OPERATOR_CLEANING_CONFLICT = "Operator cleaning conflict";

    public static final String IDEAL_END_DATE_TIME = "Ideal end date time";
    public static final String MAXIMIZE_JOBS_ASSIGNED = "Maximize jobs assigned";

    public static final String MINIMIZE_MAKESPAN = "Minimize make span";

    private PackagingScheduleConstraintProperties() {
    }
}
