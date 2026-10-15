package org.acme.schooltimetabling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class TimetableConstraintGroup {

    public static final ConstraintGroupInfo CONFLICTS = new ConstraintGroupInfo("conflicts",
            "Conflicts",
            "Never double-book a room, a teacher or a student group: each can only take one lesson per timeslot.",
            "IconCalendarX",
            new String[] { "conflicts" });

    public static final ConstraintGroupInfo TEACHER_PREFERENCES = new ConstraintGroupInfo("teacherPreferences",
            "Teacher preferences",
            "Keep each teacher in one room, with their lessons on a day as close together as possible.",
            "IconChalkboard",
            new String[] { "teacher preferences" });

    public static final ConstraintGroupInfo STUDENT_GROUP_PREFERENCES = new ConstraintGroupInfo(
            "studentGroupPreferences",
            "Student group preferences",
            "Vary the subjects a student group has in a row.",
            "IconUsersGroup",
            new String[] { "student group preferences" });

    private TimetableConstraintGroup() {
    }
}
