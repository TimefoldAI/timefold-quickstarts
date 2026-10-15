package org.acme.schooltimetabling.demo;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.RoomInputDTO;
import org.acme.schooltimetabling.dto.input.TimeslotInputDTO;
import org.acme.schooltimetabling.dto.input.TimetableInput;

/**
 * Builds the demo datasets: a {@link DemoDataSize#SMALL small} one of two student groups over two days, and a
 * {@link DemoDataSize#LARGE large} one of four student groups over a full week. Every lesson starts unassigned.
 */
public final class DemoDataBuilder {

    public enum DemoDataSize {
        SMALL,
        LARGE
    }

    private static final List<DayOfWeek> SMALL_DAYS = List.of(MONDAY, TUESDAY);
    private static final List<DayOfWeek> LARGE_DAYS = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
    /** Every day has three lessons in the morning and two in the afternoon, of one hour each. */
    private static final List<LocalTime> START_TIMES = List.of(LocalTime.of(8, 30), LocalTime.of(9, 30),
            LocalTime.of(10, 30), LocalTime.of(13, 30), LocalTime.of(14, 30));

    private static final List<String> SMALL_ROOM_NAMES = List.of("Room A", "Room B", "Room C");
    private static final List<String> LARGE_ROOM_NAMES =
            List.of("Room A", "Room B", "Room C", "Room D", "Room E", "Room F");

    private static final String TURING = "A. Turing";
    private static final String CURIE = "M. Curie";
    private static final String DARWIN = "C. Darwin";
    private static final String JONES = "I. Jones";
    private static final String CRUZ = "P. Cruz";
    private static final String DALI = "S. Dali";
    private static final String LEWIS = "C. Lewis";

    private DemoDataSize size = DemoDataSize.SMALL;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder withSize(DemoDataSize size) {
        this.size = size;
        return this;
    }

    public TimetableInput build() {
        boolean large = size == DemoDataSize.LARGE;
        return new TimetableInput(generateTimeslots(large ? LARGE_DAYS : SMALL_DAYS),
                generateRooms(large ? LARGE_ROOM_NAMES : SMALL_ROOM_NAMES), generateLessons(large));
    }

    private static List<TimeslotInputDTO> generateTimeslots(List<DayOfWeek> days) {
        List<TimeslotInputDTO> timeslots = new ArrayList<>(days.size() * START_TIMES.size());
        for (DayOfWeek day : days) {
            for (LocalTime startTime : START_TIMES) {
                timeslots.add(new TimeslotInputDTO(String.valueOf(timeslots.size()), day, startTime,
                        startTime.plusHours(1)));
            }
        }
        return List.copyOf(timeslots);
    }

    private static List<RoomInputDTO> generateRooms(List<String> roomNames) {
        List<RoomInputDTO> rooms = new ArrayList<>(roomNames.size());
        for (String roomName : roomNames) {
            rooms.add(new RoomInputDTO(String.valueOf(rooms.size()), roomName));
        }
        return List.copyOf(rooms);
    }

    private static List<LessonInputDTO> generateLessons(boolean large) {
        LessonGenerator lessons = new LessonGenerator();
        lessons.studentGroup("9th grade")
                .add(2, "Math", TURING)
                .add(1, "Physics", CURIE)
                .add(1, "Chemistry", CURIE)
                .add(1, "Biology", DARWIN)
                .add(1, "History", JONES)
                .add(2, "English", JONES)
                .add(2, "Spanish", CRUZ);
        if (large) {
            lessons.add(3, "Math", TURING)
                    .add(1, "ICT", TURING)
                    .add(1, "Physics", CURIE)
                    .add(1, "Geography", DARWIN)
                    .add(1, "Geology", DARWIN)
                    .add(1, "History", JONES)
                    .add(1, "English", JONES)
                    .add(1, "Drama", JONES)
                    .add(2, "Art", DALI)
                    .add(3, "Physical education", LEWIS);
        }
        lessons.studentGroup("10th grade")
                .add(3, "Math", TURING)
                .add(1, "Physics", CURIE)
                .add(1, "Chemistry", CURIE)
                .add(1, "French", CURIE)
                .add(1, "Geography", DARWIN)
                .add(1, "History", JONES)
                .add(1, "English", CRUZ)
                .add(1, "Spanish", CRUZ);
        if (large) {
            lessons.add(2, "Math", TURING)
                    .add(1, "ICT", TURING)
                    .add(1, "Physics", CURIE)
                    .add(1, "Biology", DARWIN)
                    .add(1, "Geology", DARWIN)
                    .add(1, "History", JONES)
                    .add(2, "English", CRUZ)
                    .add(1, "Drama", JONES)
                    .add(2, "Art", DALI)
                    .add(3, "Physical education", LEWIS);
            for (String studentGroup : List.of("11th grade", "12th grade")) {
                lessons.studentGroup(studentGroup)
                        .add(5, "Math", TURING)
                        .add(1, "ICT", TURING)
                        .add(1, "Physics", CURIE)
                        .add(1, "Chemistry", CURIE)
                        .add(1, "French", CURIE)
                        .add(1, "Physics", CURIE)
                        .add(1, "Geography", DARWIN)
                        .add(1, "Biology", DARWIN)
                        .add(1, "Geology", DARWIN)
                        .add(2, "History", JONES)
                        .add(3, "English", CRUZ)
                        .add(1, "Spanish", CRUZ)
                        .add(1, "Drama", CRUZ)
                        .add(2, "Art", DALI)
                        .add(3, "Physical education", LEWIS);
            }
        }
        return List.copyOf(lessons.lessons);
    }

    /**
     * Collects the lessons of one student group after another, numbering them in the order they are added.
     */
    private static final class LessonGenerator {

        private final List<LessonInputDTO> lessons = new ArrayList<>();
        private String studentGroup;

        LessonGenerator studentGroup(String studentGroup) {
            this.studentGroup = studentGroup;
            return this;
        }

        LessonGenerator add(int count, String subject, String teacher) {
            for (int i = 0; i < count; i++) {
                lessons.add(new LessonInputDTO(String.valueOf(lessons.size()), subject, teacher, studentGroup, null,
                        null));
            }
            return this;
        }
    }
}
