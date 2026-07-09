package org.acme.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.schooltimetabling.dto.LessonDTO;
import org.acme.schooltimetabling.dto.RoomDTO;
import org.acme.schooltimetabling.dto.TimeslotDTO;
import org.acme.schooltimetabling.dto.TimetableInput;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static TimetableInput createProblem() {
        List<TimeslotDTO> timeslots = new ArrayList<>();
        LocalTime[] starts = { LocalTime.of(8, 30), LocalTime.of(9, 30), LocalTime.of(10, 30), LocalTime.of(13, 30),
                LocalTime.of(14, 30) };
        DayOfWeek[] days = { DayOfWeek.MONDAY, DayOfWeek.TUESDAY };
        long timeslotId = 0;
        for (DayOfWeek day : days) {
            for (LocalTime start : starts) {
                timeslots.add(new TimeslotDTO(Long.toString(timeslotId++), day.name(), start.toString(),
                        start.plusHours(1).toString()));
            }
        }

        List<RoomDTO> rooms = new ArrayList<>();
        rooms.add(new RoomDTO("0", "Room A"));
        rooms.add(new RoomDTO("1", "Room B"));
        rooms.add(new RoomDTO("2", "Room C"));

        String[][] lessonDefinitions = {
                { "Math", "A. Turing", "9th grade" }, { "Math", "A. Turing", "9th grade" },
                { "Physics", "M. Curie", "9th grade" }, { "Chemistry", "M. Curie", "9th grade" },
                { "Biology", "C. Darwin", "9th grade" }, { "History", "I. Jones", "9th grade" },
                { "English", "I. Jones", "9th grade" }, { "Spanish", "P. Cruz", "9th grade" },
                { "Math", "A. Turing", "10th grade" }, { "Physics", "M. Curie", "10th grade" },
                { "Chemistry", "M. Curie", "10th grade" }, { "French", "M. Curie", "10th grade" },
                { "Geography", "C. Darwin", "10th grade" }, { "History", "I. Jones", "10th grade" },
                { "English", "P. Cruz", "10th grade" }, { "Spanish", "P. Cruz", "10th grade" }
        };
        List<LessonDTO> lessons = new ArrayList<>();
        long lessonId = 0;
        for (String[] definition : lessonDefinitions) {
            lessons.add(new LessonDTO(Long.toString(lessonId++), definition[0], definition[1], definition[2], "", ""));
        }

        return new TimetableInput(lessons, timeslots, rooms);
    }
}
