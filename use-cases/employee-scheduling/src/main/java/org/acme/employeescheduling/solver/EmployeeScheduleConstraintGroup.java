package org.acme.employeescheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class EmployeeScheduleConstraintGroup {

    public static final ConstraintGroupInfo SKILLS = new ConstraintGroupInfo("skills",
            "Skills",
            "Only assign an employee to a shift that needs a skill they have.",
            "IconCertificate",
            new String[] { "skills" });

    public static final ConstraintGroupInfo WORKING_TIME = new ConstraintGroupInfo("workingTime",
            "Working time",
            "Never give an employee overlapping shifts, more than one shift a day, or too little rest between shifts.",
            "IconClockHour4",
            new String[] { "working time" });

    public static final ConstraintGroupInfo AVAILABILITY = new ConstraintGroupInfo("availability",
            "Availability",
            "Respect the days an employee cannot work, would rather not work, or would like to work.",
            "IconCalendarUser",
            new String[] { "availability" });

    public static final ConstraintGroupInfo FAIRNESS = new ConstraintGroupInfo("fairness",
            "Fairness",
            "Spread the shifts evenly across all employees.",
            "IconScale",
            new String[] { "fairness" });

    private EmployeeScheduleConstraintGroup() {
    }
}
