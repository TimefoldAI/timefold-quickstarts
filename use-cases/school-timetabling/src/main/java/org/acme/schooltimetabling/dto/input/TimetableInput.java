package org.acme.schooltimetabling.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The school timetabling planning problem input.")
public record TimetableInput(
        @Schema(description = "Timeslots of the weekly timetable lessons can be scheduled in.", required = true,
                minItems = 1) List<TimeslotInputDTO> timeslots,
        @Schema(description = "Rooms lessons can be taught in.", required = true, minItems = 1) List<RoomInputDTO> rooms,
        @Schema(description = "Lessons to assign a timeslot and a room to.", required = true,
                minItems = 1) List<LessonInputDTO> lessons)
        implements
            ModelInput {

    public TimetableInput withLessons(List<LessonInputDTO> lessons) {
        return new TimetableInput(timeslots, rooms, lessons);
    }
}
