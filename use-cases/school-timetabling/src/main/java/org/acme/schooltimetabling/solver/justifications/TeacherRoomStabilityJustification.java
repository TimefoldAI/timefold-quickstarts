package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.schooltimetabling.domain.Lesson;

public record TeacherRoomStabilityJustification(String teacher, Lesson lesson1, Lesson lesson2) implements ConstraintJustification {

}
