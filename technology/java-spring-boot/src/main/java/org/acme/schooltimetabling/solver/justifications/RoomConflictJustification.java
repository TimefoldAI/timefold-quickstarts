package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import org.acme.schooltimetabling.domain.Room;

public record RoomConflictJustification(Room room, long lessonId1, long lessonId2)
        implements ConstraintJustification {

}
