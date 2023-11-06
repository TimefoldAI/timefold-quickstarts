package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record TeacherRoomStabilityJustification(String teacher, long lessonId1, long lessonId2)
        implements ConstraintJustification {

}
