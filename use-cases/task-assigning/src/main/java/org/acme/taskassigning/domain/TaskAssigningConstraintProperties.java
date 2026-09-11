package org.acme.taskassigning.domain;

public final class TaskAssigningConstraintProperties {

    public static final String NO_MISSING_SKILLS = "No missing skills";
    public static final String MINIMIZE_UNASSIGNED_TASKS = "Minimize unassigned tasks";
    public static final String MINIMIZE_MAKESPAN = "Minimize makespan";
    public static final String CRITICAL_PRIORITY_TASK_END_TIME = "Critical priority task end time";
    public static final String MAJOR_PRIORITY_TASK_END_TIME = "Major priority task end time";
    public static final String MINOR_PRIORITY_TASK_END_TIME = "Minor priority task end time";

    private TaskAssigningConstraintProperties() {
    }
}
