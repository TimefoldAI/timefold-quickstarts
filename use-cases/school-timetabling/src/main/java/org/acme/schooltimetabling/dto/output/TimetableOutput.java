package org.acme.schooltimetabling.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The school timetabling planning problem output.")
public record TimetableOutput(
        @Schema(description = "Lessons with the timeslot and room they are scheduled in.", required = true,
                minItems = 1) List<LessonOutputDTO> lessons)
        implements
            ModelOutput {
}
