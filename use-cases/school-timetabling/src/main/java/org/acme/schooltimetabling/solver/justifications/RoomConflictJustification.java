package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;

public record RoomConflictJustification(Room room, Lesson lesson1, Lesson lesson2) implements ConstraintJustification {

}
