package org.acme.schooltimetabling;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TimetableApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableApp.class);

    public enum DemoData {
        SMALL,
        LARGE
    }

    public static void main(String[] args) {
        SolverFactory<Timetable> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(Timetable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(5)));

        // Load the problem
        Timetable problem = generateDemoData(DemoData.SMALL);

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);

        // Visualize the solution
        printTimetable(solution);
    }

    public static Timetable generateDemoData(DemoData demoData) {
        List<Timeslot> timeslots = new ArrayList<>(10);
        long nextTimeslotId = 0L;
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
        if (demoData == DemoData.LARGE) {
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId), DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
        }

        List<Room> rooms = new ArrayList<>(3);
        long nextRoomId = 0L;
        rooms.add(new Room(Long.toString(nextRoomId++), "Room A"));
        rooms.add(new Room(Long.toString(nextRoomId++), "Room B"));
        rooms.add(new Room(Long.toString(nextRoomId++), "Room C"));
        if (demoData == DemoData.LARGE) {
            rooms.add(new Room(Long.toString(nextRoomId++), "Room D"));
            rooms.add(new Room(Long.toString(nextRoomId++), "Room E"));
            rooms.add(new Room(Long.toString(nextRoomId), "Room F"));
        }

        List<Lesson> lessons = new ArrayList<>();
        long nextLessonId = 0L;
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "9th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "9th grade"));
        if (demoData == DemoData.LARGE) {
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "I. Jones", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
        }

        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "French", "M. Curie", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
        lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "10th grade"));
        if (demoData == DemoData.LARGE) {
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "I. Jones", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));

            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "French", "M. Curie", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));

            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "French", "M. Curie", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
            lessons.add(new Lesson(Long.toString(nextLessonId), "Physical education", "C. Lewis", "12th grade"));
        }
        return new Timetable(demoData.name(), timeslots, rooms, lessons);
    }

    private static void printTimetable(Timetable timeTable) {
        LOGGER.info("");
        List<Room> rooms = timeTable.getRooms();
        List<Lesson> lessons = timeTable.getLessons();
        Map<Timeslot, Map<Room, List<Lesson>>> lessonMap = lessons.stream()
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getRoom() != null)
                .collect(Collectors.groupingBy(Lesson::getTimeslot, Collectors.groupingBy(Lesson::getRoom)));
        LOGGER.info("|            | " + rooms.stream()
                .map(room -> String.format("%-10s", room.getName())).collect(Collectors.joining(" | ")) + " |");
        LOGGER.info("|" + "------------|".repeat(rooms.size() + 1));
        for (Timeslot timeslot : timeTable.getTimeslots()) {
            List<List<Lesson>> cells = rooms.stream()
                    .map(room -> {
                        Map<Room, List<Lesson>> byRoomMap = lessonMap.get(timeslot);
                        if (byRoomMap == null) {
                            return Collections.<Lesson>emptyList();
                        }
                        List<Lesson> cellLessons = byRoomMap.get(room);
                        return Objects.requireNonNullElse(cellLessons, Collections.<Lesson>emptyList());
                    }).toList();

            LOGGER.info("| " + String.format("%-10s",
                    timeslot.getDayOfWeek().toString().substring(0, 3) + " " + timeslot.getStartTime()) + " | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(Lesson::getSubject).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
            LOGGER.info("|            | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(Lesson::getTeacher).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
            LOGGER.info("|            | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(Lesson::getStudentGroup).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
            LOGGER.info("|" + "------------|".repeat(rooms.size() + 1));
        }
        List<Lesson> unassignedLessons = lessons.stream()
                .filter(lesson -> lesson.getTimeslot() == null || lesson.getRoom() == null)
                .toList();
        if (!unassignedLessons.isEmpty()) {
            LOGGER.info("");
            LOGGER.info("Unassigned lessons");
            for (Lesson lesson : unassignedLessons) {
                LOGGER.info("  " + lesson.getSubject() + " - " + lesson.getTeacher() + " - " + lesson.getStudentGroup());
            }
        }
    }

}
