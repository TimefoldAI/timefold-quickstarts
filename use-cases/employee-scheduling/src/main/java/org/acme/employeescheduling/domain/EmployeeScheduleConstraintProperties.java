package org.acme.employeescheduling.domain;

public final class EmployeeScheduleConstraintProperties {

    public static final String MISSING_REQUIRED_SKILL = "Missing required skill";
    public static final String OVERLAPPING_SHIFT = "Overlapping shift";
    public static final String AT_LEAST_10_HOURS_BETWEEN_TWO_SHIFTS = "At least 10 hours between 2 shifts";
    public static final String MAX_ONE_SHIFT_PER_DAY = "Max one shift per day";
    public static final String UNAVAILABLE_EMPLOYEE = "Unavailable employee";

    public static final String UNDESIRED_DAY_FOR_EMPLOYEE = "Undesired day for employee";
    public static final String DESIRED_DAY_FOR_EMPLOYEE = "Desired day for employee";
    public static final String BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS = "Balance employee shift assignments";

    private EmployeeScheduleConstraintProperties() {
    }
}
