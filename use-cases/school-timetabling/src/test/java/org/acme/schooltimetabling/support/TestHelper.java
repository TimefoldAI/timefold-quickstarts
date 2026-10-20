package org.acme.schooltimetabling.support;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.RoomInputDTO;
import org.acme.schooltimetabling.dto.input.TimeslotInputDTO;
import org.acme.schooltimetabling.dto.input.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.input.TimetableInput;
import org.acme.schooltimetabling.dto.output.LessonOutputDTO;
import org.acme.schooltimetabling.dto.output.TimetableOutput;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private TestHelper() {
    }

    // ************************************************************************
    // DTOs
    // ************************************************************************

    public static TimetableInput input(List<TimeslotInputDTO> timeslots, List<RoomInputDTO> rooms,
            List<LessonInputDTO> lessons) {
        return new TimetableInput(timeslots, rooms, lessons);
    }

    /**
     * A small but feasible problem: four timeslots on Monday, two rooms and four lessons, taught by two
     * teachers to two student groups.
     */
    public static TimetableInput createProblem() {
        List<TimeslotInputDTO> timeslots = List.of(
                timeslotDTO("t1", DayOfWeek.MONDAY, LocalTime.of(8, 30)),
                timeslotDTO("t2", DayOfWeek.MONDAY, LocalTime.of(9, 30)),
                timeslotDTO("t3", DayOfWeek.MONDAY, LocalTime.of(10, 30)),
                timeslotDTO("t4", DayOfWeek.MONDAY, LocalTime.of(13, 30)));
        List<RoomInputDTO> rooms = List.of(roomDTO("r1"), roomDTO("r2"));
        List<LessonInputDTO> lessons = List.of(
                aLessonDTO("l1").subject("Math").teacher("A. Turing").studentGroup("9th grade").build(),
                aLessonDTO("l2").subject("Physics").teacher("M. Curie").studentGroup("9th grade").build(),
                aLessonDTO("l3").subject("Math").teacher("A. Turing").studentGroup("10th grade").build(),
                aLessonDTO("l4").subject("Chemistry").teacher("M. Curie").studentGroup("10th grade").build());
        return input(timeslots, rooms, lessons);
    }

    public static TimetableConfigOverrides configOverrides(Long teacherRoomStabilityWeight,
            Long teacherTimeEfficiencyWeight, Long studentGroupSubjectVarietyWeight) {
        return new TimetableConfigOverrides(teacherRoomStabilityWeight, teacherTimeEfficiencyWeight,
                studentGroupSubjectVarietyWeight);
    }

    /** A one-hour timeslot. */
    public static TimeslotInputDTO timeslotDTO(String id, DayOfWeek dayOfWeek, LocalTime startTime) {
        return timeslotDTO(id, dayOfWeek, startTime, startTime.plusHours(1));
    }

    public static TimeslotInputDTO timeslotDTO(String id, DayOfWeek dayOfWeek, LocalTime startTime,
            LocalTime endTime) {
        return new TimeslotInputDTO(id, dayOfWeek, startTime, endTime);
    }

    public static RoomInputDTO roomDTO(String id) {
        return new RoomInputDTO(id, "Room " + id);
    }

    public static LessonDTOBuilder aLessonDTO(String id) {
        return new LessonDTOBuilder(id);
    }

    public static LessonOutputDTO lessonOutputDTO(String id, String timeslotId, String roomId) {
        return new LessonOutputDTO(id, timeslotId, roomId);
    }

    public static TimetableOutput output(List<LessonOutputDTO> lessons) {
        return new TimetableOutput(lessons);
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    /** A one-hour timeslot. */
    public static Timeslot timeslot(String id, DayOfWeek dayOfWeek, LocalTime startTime) {
        return new Timeslot(id, dayOfWeek, startTime, startTime.plusHours(1));
    }

    public static Room room(String id) {
        return new Room(id, "Room " + id);
    }

    public static LessonBuilder aLesson(String id) {
        return new LessonBuilder(id);
    }

    public static final class LessonBuilder {

        private final String id;
        private String subject = "Subject";
        private String teacher = "Teacher";
        private String studentGroup = "Group";
        private Timeslot timeslot;
        private Room room;

        private LessonBuilder(String id) {
            this.id = id;
        }

        public LessonBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public LessonBuilder teacher(String teacher) {
            this.teacher = teacher;
            return this;
        }

        public LessonBuilder studentGroup(String studentGroup) {
            this.studentGroup = studentGroup;
            return this;
        }

        public LessonBuilder timeslot(Timeslot timeslot) {
            this.timeslot = timeslot;
            return this;
        }

        public LessonBuilder room(Room room) {
            this.room = room;
            return this;
        }

        public Lesson build() {
            return new Lesson(id, subject, teacher, studentGroup, timeslot, room);
        }
    }

    public static final class LessonDTOBuilder {

        private final String id;
        private String subject = "Subject";
        private String teacher = "Teacher";
        private String studentGroup = "Group";
        private String timeslotId;
        private String roomId;

        private LessonDTOBuilder(String id) {
            this.id = id;
        }

        public LessonDTOBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public LessonDTOBuilder teacher(String teacher) {
            this.teacher = teacher;
            return this;
        }

        public LessonDTOBuilder studentGroup(String studentGroup) {
            this.studentGroup = studentGroup;
            return this;
        }

        public LessonDTOBuilder timeslotId(String timeslotId) {
            this.timeslotId = timeslotId;
            return this;
        }

        public LessonDTOBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public LessonInputDTO build() {
            return new LessonInputDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
        }
    }
}
