package org.acme.schooltimetabling.rest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo school timetable data.")
@Path("demo-data")
public class TimetableDemoResource {

    public enum DemoData {
        SMALL,
        LARGE
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of demo data represented as IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DemoData.class, type = SchemaType.ARRAY))) })
    @Operation(summary = "List demo data.")
    @GET
    public DemoData[] list() {
        return DemoData.values();
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo timetable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Timetable.class)))})
    @Operation(summary = "Find an unsolved demo timetable by ID.")
    @GET
    @Path("/{demoDataId}")
    public Response generate(@Parameter(description = "Unique identifier of the demo data.",
            required = true) @PathParam("demoDataId") DemoData demoData) {
        List<Timeslot> timeslotList = new ArrayList<>(10);
        long nextTimeslotId = 0L;
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
        if (demoData == DemoData.LARGE) {
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
        }

        List<Room> roomList = new ArrayList<>(3);
        long nextRoomId = 0L;
        roomList.add(new Room(nextRoomId++, "Room A"));
        roomList.add(new Room(nextRoomId++, "Room B"));
        roomList.add(new Room(nextRoomId++, "Room C"));
        if (demoData == DemoData.LARGE) {
            roomList.add(new Room(nextRoomId++, "Room D"));
            roomList.add(new Room(nextRoomId++, "Room E"));
            roomList.add(new Room(nextRoomId++, "Room F"));
        }

        List<Lesson> lessonList = new ArrayList<>();
        long nextLessonId = 0L;
        lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Chemistry", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "9th grade"));
        if (demoData == DemoData.LARGE) {
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "I. Jones", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Drama", "I. Jones", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade"));
        }

        lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Chemistry", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "French", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "10th grade"));
        if (demoData == DemoData.LARGE) {
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Drama", "I. Jones", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "10th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "10th grade"));

            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Chemistry", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "French", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Drama", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "11th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "11th grade"));

            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Chemistry", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "French", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "History", "I. Jones", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Drama", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Art", "S. Dali", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "12th grade"));
            lessonList.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "12th grade"));
        }
        return Response.ok(new Timetable(demoData.name(), timeslotList, roomList, lessonList)).build();
    }

}
