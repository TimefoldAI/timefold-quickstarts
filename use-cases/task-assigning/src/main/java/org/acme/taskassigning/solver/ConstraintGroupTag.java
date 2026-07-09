package org.acme.taskassigning.solver;

public enum ConstraintGroupTag {
    SKILLS("skills"),
    TASK_ASSIGNMENT("task assignment"),
    MAKESPAN("makespan"),
    PRIORITY("priority");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
