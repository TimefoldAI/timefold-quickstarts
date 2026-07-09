package org.acme.schooltimetabling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The school timetabling planning problem output.")
public record TimetableOutput(
        @Schema(description = "List of lessons with their assigned timeslot and room.") List<LessonDTO> lessons,
        @Schema(description = "List of timeslots a lesson can be assigned to.") List<TimeslotDTO> timeslots,
        @Schema(description = "List of rooms a lesson can be assigned to.") List<RoomDTO> rooms,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public TimetableOutput {
        lessons = List.copyOf(lessons);
        timeslots = List.copyOf(timeslots);
        rooms = List.copyOf(rooms);
    }

    public TimetableOutput withLessons(List<LessonDTO> lessons) {
        return new TimetableOutput(lessons, timeslots, rooms, score);
    }

    public TimetableOutput withTimeslots(List<TimeslotDTO> timeslots) {
        return new TimetableOutput(lessons, timeslots, rooms, score);
    }

    public TimetableOutput withRooms(List<RoomDTO> rooms) {
        return new TimetableOutput(lessons, timeslots, rooms, score);
    }

    public TimetableOutput withScore(String score) {
        return new TimetableOutput(lessons, timeslots, rooms, score);
    }
}
