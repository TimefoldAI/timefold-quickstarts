package org.acme.taskassigning.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class TaskAssigningConstraintGroup {
    public static final ConstraintGroupInfo SKILLS = new ConstraintGroupInfo("skills",
            "Skills",
            "Ensure every task is performed by an employee who has all the required skills.",
            "IconCertificate",
            new String[] { ConstraintGroupTag.SKILLS.getTag() });
    public static final ConstraintGroupInfo TASK_ASSIGNMENT = new ConstraintGroupInfo("taskAssignment",
            "Task assignment",
            "Assign as many tasks as possible to employees.",
            "IconChecklist",
            new String[] { ConstraintGroupTag.TASK_ASSIGNMENT.getTag() });
    public static final ConstraintGroupInfo MAKESPAN = new ConstraintGroupInfo("makespan",
            "Makespan",
            "Balance the workload so the latest finishing employee finishes as early as possible.",
            "IconClock",
            new String[] { ConstraintGroupTag.MAKESPAN.getTag() });
    public static final ConstraintGroupInfo PRIORITY = new ConstraintGroupInfo("priority",
            "Priority",
            "Finish higher priority tasks earlier than lower priority tasks.",
            "IconFlag",
            new String[] { ConstraintGroupTag.PRIORITY.getTag() });

    private TaskAssigningConstraintGroup() {
    }
}
