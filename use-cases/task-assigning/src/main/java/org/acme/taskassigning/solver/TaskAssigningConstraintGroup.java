package org.acme.taskassigning.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class TaskAssigningConstraintGroup {

    public static final ConstraintGroupInfo SKILLS = new ConstraintGroupInfo("skills",
            "Skills",
            "Make sure an employee is only assigned tasks they have the required skills for.",
            "IconTool",
            new String[] { "skills" });

    public static final ConstraintGroupInfo WORKLOAD = new ConstraintGroupInfo("workload",
            "Workload",
            "Assign every task to an employee, and keep the latest-finishing employee's day as short as possible.",
            "IconBriefcase",
            new String[] { "workload", "makespan" });

    public static final ConstraintGroupInfo TASK_PRIORITY = new ConstraintGroupInfo("taskPriority",
            "Task priority",
            "Finish higher priority tasks earlier than lower priority ones.",
            "IconFlag3",
            new String[] { "priority" });

    private TaskAssigningConstraintGroup() {
    }
}
