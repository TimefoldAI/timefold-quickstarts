package org.acme.employeescheduling.solver;

public enum ConstraintGroupTag {
    SHIFT_COVERAGE("shift coverage"),
    EMPLOYEE_AVAILABILITY("employee availability"),
    WORKLOAD_BALANCE("workload balance");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
