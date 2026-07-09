package org.acme.schooltimetabling.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    TEACHER_SATISFACTION("teacher satisfaction"),
    STUDENT_SATISFACTION("student satisfaction");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
