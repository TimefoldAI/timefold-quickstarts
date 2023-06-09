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
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data sets", description = "Timefold-provided demo school timetable data sets.")
@Path("demo/datasets")
public class TimeTableDemoResource {

    public enum DemoDataSet {
        SMALL,
        LARGE
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of demo data sets represented as IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DemoDataSet.class, type = SchemaType.ARRAY))) })
    @Operation(summary = "List demo data sets.")
    @GET
    public DemoDataSet[] list() {
        return DemoDataSet.values();
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo timetable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TimeTable.class))),
            @APIResponse(responseCode = "404", description = "Demo data set does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN))})
    @Operation(summary = "Find an unsolved demo timetable by ID.")
    @GET
    @Path("/{dataSetId}")
    public Response generate(@Parameter(description = "Unique identifier of the demo data set.",
            required = true) @PathParam("dataSetId") String dataSetId) {
        DemoDataSet demoDataSet;
        try {
            demoDataSet = DemoDataSet.valueOf(dataSetId);
        } catch (IllegalArgumentException illegalArgumentException) {
            return Response.status(Response.Status.NOT_FOUND).entity("Demo data set " + dataSetId + " not found.").build();
        }

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
        if (demoDataSet == DemoDataSet.LARGE) {
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
        if (demoDataSet == DemoDataSet.LARGE) {
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
        if (demoDataSet == DemoDataSet.LARGE) {
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
        if (demoDataSet == DemoDataSet.LARGE) {
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
        return Response.ok(new TimeTable(demoDataSet.name(), timeslotList, roomList, lessonList)).build();
    }

}
