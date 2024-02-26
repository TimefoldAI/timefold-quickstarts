package org.acme.schooltimetabling.rest;

//import java.time.DayOfWeek;
import java.time.LocalDate; //added
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
        List<Timeslot> timeslots = new ArrayList<>(10);
        long nextTimeslotId = 0L;
       /*  timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30))); */

        timeslots.add(new Timeslot(nextTimeslotId++, LocalDate.of(2024, 6, 15), LocalTime.of(7, 30), LocalTime.of(8, 00)));
        timeslots.add(new Timeslot(nextTimeslotId++, LocalDate.of(2024, 6, 15), LocalTime.of(11, 30), LocalTime.of(12, 00)));
        timeslots.add(new Timeslot(nextTimeslotId++, LocalDate.of(2024, 6, 15), LocalTime.of(5, 30), LocalTime.of(6, 00)));
        


        if (demoData == DemoData.LARGE) {
          /* timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
            timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30))); */
        }

        List<Room> rooms = new ArrayList<>(3);
        long nextRoomId = 0L;
        rooms.add(new Room(nextRoomId++, "Day 1"));
        rooms.add(new Room(nextRoomId++, "Day 2"));
        rooms.add(new Room(nextRoomId++, "Day 3"));
        if (demoData == DemoData.LARGE) {
            rooms.add(new Room(nextRoomId++, "Room D"));
            rooms.add(new Room(nextRoomId++, "Room E"));
            rooms.add(new Room(nextRoomId++, "Room F"));
        }

        List<Lesson> lessons = new ArrayList<>();
        long nextLessonId = 0L;
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Turing",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Turing",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Turing",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Turing",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Turing",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Johnson", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Johnson", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Johnson", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Johnson", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Johnson", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Curie",   "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Curie",   "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Curie",   "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Curie",   "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Curie",   "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Smith",   "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Smith",   "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Smith",   "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Smith",   "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Smith",   "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Darwin",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Jones",   "Group C"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Conner",  "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Allen",   "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Gorilla", "Cruz",    "Group C"));
        lessons.add(new Lesson(nextLessonId++, "Gorilla", "Phillip", "Group B"));
        if (demoData == DemoData.LARGE) {
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "I. Jones", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Drama", "I. Jones", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade"));
        }

        lessons.add(new Lesson(nextLessonId++, "Monkeys",  "Engle", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",  "Clyde", "Group D"));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",  "Chost", "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",     "Olson", "Group C"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",     "Marti", "Group D"));
        lessons.add(new Lesson(nextLessonId++, "Dogs",     "Stant", "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Gorillas", "Chips", "Group A"));
        lessons.add(new Lesson(nextLessonId++, "Gorillas", "Harve", "Group B"));
        lessons.add(new Lesson(nextLessonId++, "Gorillas", "Kline", "Group D"));
        lessons.add(new Lesson(nextLessonId++, "Gorillas", "Aaron", "Group A"));
        if (demoData == DemoData.LARGE) {
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Drama", "I. Jones", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "10th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "10th grade"));

            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Chemistry", "M. Curie", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "French", "M. Curie", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Drama", "P. Cruz", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "11th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "11th grade"));

            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Turing", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "ICT", "A. Turing", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Chemistry", "M. Curie", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "French", "M. Curie", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Biology", "C. Darwin", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Geology", "C. Darwin", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "English", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Spanish", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Drama", "P. Cruz", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "12th grade"));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "12th grade"));
        }
        return Response.ok(new Timetable(demoData.name(), timeslots, rooms, lessons)).build();
    }

}
