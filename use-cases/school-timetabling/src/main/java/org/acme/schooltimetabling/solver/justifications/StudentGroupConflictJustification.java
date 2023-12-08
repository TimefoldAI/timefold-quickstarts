package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import org.acme.schooltimetabling.domain.Lesson;

public record StudentGroupConflictJustification(String studentGroup, Lesson lesson1, Lesson lesson2, String description)
        implements ConstraintJustification {
    public StudentGroupConflictJustification(String studentGroup, Lesson lesson1, Lesson lesson2) {
        this(studentGroup, lesson1, lesson2, String.format("Student group '%s' has lesson '%s' and lesson '%s' at '%s %s'", studentGroup, lesson1.getSubject(), lesson2.getSubject(), lesson1.getTimeslot().getDayOfWeek(), lesson1.getTimeslot().getStartTime()));
    }
}
