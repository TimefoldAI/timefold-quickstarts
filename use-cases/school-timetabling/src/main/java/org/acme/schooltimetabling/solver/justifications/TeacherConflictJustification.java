package org.acme.schooltimetabling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record TeacherConflictJustification(String teacher, long lessonId1, long lessonId2)
        implements ConstraintJustification {

}
