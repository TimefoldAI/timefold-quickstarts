package org.acme.schooltimetabling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The school timetabling planning problem input.")
public record TimetableInput(
        @Schema(description = "List of lessons that must each be assigned to a timeslot and a room.") List<LessonDTO> lessons,
        @Schema(description = "List of timeslots a lesson can be assigned to.") List<TimeslotDTO> timeslots,
        @Schema(description = "List of rooms a lesson can be assigned to.") List<RoomDTO> rooms) implements ModelInput {

    public TimetableInput {
        lessons = List.copyOf(lessons);
        timeslots = List.copyOf(timeslots);
        rooms = List.copyOf(rooms);
    }

    public TimetableInput withLessons(List<LessonDTO> lessons) {
        return new TimetableInput(lessons, timeslots, rooms);
    }

    public TimetableInput withTimeslots(List<TimeslotDTO> timeslots) {
        return new TimetableInput(lessons, timeslots, rooms);
    }

    public TimetableInput withRooms(List<RoomDTO> rooms) {
        return new TimetableInput(lessons, timeslots, rooms);
    }
}
