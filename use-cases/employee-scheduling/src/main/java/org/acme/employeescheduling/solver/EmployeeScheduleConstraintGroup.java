package org.acme.employeescheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class EmployeeScheduleConstraintGroup {

    public static final ConstraintGroupInfo SHIFT_COVERAGE = new ConstraintGroupInfo("shiftCoverage",
            "Shift coverage",
            "Ensure every shift is covered by a qualified employee without conflicts.",
            "IconCalendar",
            new String[] { ConstraintGroupTag.SHIFT_COVERAGE.getTag() });

    public static final ConstraintGroupInfo EMPLOYEE_AVAILABILITY = new ConstraintGroupInfo("employeeAvailability",
            "Employee availability",
            "Respect employee unavailability, undesired dates and desired dates.",
            "IconUser",
            new String[] { ConstraintGroupTag.EMPLOYEE_AVAILABILITY.getTag() });

    public static final ConstraintGroupInfo WORKLOAD_BALANCE = new ConstraintGroupInfo("workloadBalance",
            "Workload balance",
            "Distribute shifts fairly across all employees.",
            "IconScale",
            new String[] { ConstraintGroupTag.WORKLOAD_BALANCE.getTag() });

    private EmployeeScheduleConstraintGroup() {
    }
}
