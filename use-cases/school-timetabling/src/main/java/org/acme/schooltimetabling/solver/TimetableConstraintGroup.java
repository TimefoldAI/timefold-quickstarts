package org.acme.schooltimetabling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class TimetableConstraintGroup {
    public static final ConstraintGroupInfo CONFLICT_AVOIDANCE = new ConstraintGroupInfo("conflictAvoidance",
            "Conflict avoidance",
            "Ensure no room, teacher or student group is double-booked in the same timeslot.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo TEACHER_PREFERENCES = new ConstraintGroupInfo("teacherPreferences",
            "Teacher preferences",
            "Keep teachers in a stable room and group their lessons together.",
            "IconUser",
            new String[] { ConstraintGroupTag.TEACHER_SATISFACTION.getTag() });
    public static final ConstraintGroupInfo STUDENT_PREFERENCES = new ConstraintGroupInfo("studentPreferences",
            "Student preferences",
            "Spread the same subject out across a student group's day.",
            "IconBook",
            new String[] { ConstraintGroupTag.STUDENT_SATISFACTION.getTag() });

    private TimetableConstraintGroup() {
    }
}
