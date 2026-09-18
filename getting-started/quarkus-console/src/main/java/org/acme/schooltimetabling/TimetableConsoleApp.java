package org.acme.schooltimetabling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.console.api.SolverConsole;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.quarkus.jackson.TimefoldJacksonModule;
import ai.timefold.solver.quarkus.jackson.solution.JacksonSolutionFileIO;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * The console-dashboard sibling of {@code school-timetabling}: same domain, same constraints,
 * same {@code timefold-solver-quarkus} wiring, but watched live via {@link SolverConsole} instead
 * of a REST API and web UI.
 */
@QuarkusMain
public class TimetableConsoleApp implements QuarkusApplication {

    public enum DemoData {
        SMALL,
        LARGE
    }

    @Inject
    SolverFactory<Timetable> solverFactory;

    @Override
    public int run(String... args) {
        Path outputFile = parseOutputFile(args);

        // Load the problem
        Timetable problem = generateDemoData(DemoData.LARGE);

        // Solve the problem, live dashboard included; SolverConsole prints its own final summary
        Timetable solution = SolverConsole.solve(solverFactory, problem);

        if (outputFile != null) {
            writeSolution(solution, outputFile);
            System.out.println();
            System.out.println("Solution written to " + outputFile.toAbsolutePath() + ".");
        }
        return 0;
    }

    private static Path parseOutputFile(String... args) {
        if (args.length == 0) {
            return null;
        }
        var outputFile = Path.of(args[0]);
        var parentDirectory = outputFile.toAbsolutePath().getParent();
        if (parentDirectory != null && !Files.isDirectory(parentDirectory)) {
            throw new IllegalArgumentException(
                    "The parentDirectory (%s) of the outputFile (%s) does not exist."
                            .formatted(parentDirectory, outputFile));
        }
        return outputFile;
    }

    private static void writeSolution(Timetable solution, Path outputFile) {
        // Jackson's own datatype-jsr310 module is picked up by findAndRegisterModules() below, but
        // its WRITE_DATES_AS_TIMESTAMPS default must be disabled first, or LocalTime serializes as
        // a [hour, minute, second] array instead of "08:30". TimefoldJacksonModule is registered
        // explicitly (rather than relying on findAndRegisterModules()'s ServiceLoader lookup alone)
        // so the score still serializes correctly under GraalVM native image, where ServiceLoader
        // discovery of Jackson modules is not registered by default.
        var mapper = JsonMapper.builder()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.registerModule(TimefoldJacksonModule.createModule());
        new JacksonSolutionFileIO<Timetable>(Timetable.class, mapper).write(solution, outputFile.toFile());
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

}
