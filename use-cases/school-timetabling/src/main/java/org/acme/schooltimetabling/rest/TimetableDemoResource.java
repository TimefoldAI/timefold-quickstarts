package org.acme.schooltimetabling.rest;

//import java.time.DayOfWeek;
import java.time.LocalDate; //added
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        List<Timeslot> timeslots = new ArrayList<>(3);
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
        timeslots.add(new Timeslot(nextTimeslotId++, DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30))); 

        timeslots.add(new Timeslot(nextTimeslotId++, LocalDate.of(2024, 6, 15), LocalTime.of(7, 30), LocalTime.of(8, 00)));
        timeslots.add(new Timeslot(nextTimeslotId++, LocalDate.of(2024, 6, 15), LocalTime.of(11, 30), LocalTime.of(12, 00)));
        timeslots.add(new Timeslot(nextTimeslotId++, LocalDate.of(2024, 6, 15), LocalTime.of(5, 30), LocalTime.of(6, 00)));*/

        timeslots.add(new Timeslot(nextTimeslotId++, "Breakfast"));
        timeslots.add(new Timeslot(nextTimeslotId++, "Lunch"));
        timeslots.add(new Timeslot(nextTimeslotId++, "Supper"));
        


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

        List<Room> rooms = new ArrayList<>(6);
        long nextRoomId = 0L;
        rooms.add(new Room(nextRoomId++, "Day 1"));
        rooms.add(new Room(nextRoomId++, "Day 2"));
        rooms.add(new Room(nextRoomId++, "Day 3"));
        rooms.add(new Room(nextRoomId++, "Day 4"));
        rooms.add(new Room(nextRoomId++, "Day 5"));
        rooms.add(new Room(nextRoomId++, "Day 6"));
        rooms.add(new Room(nextRoomId++, "Day 7"));
        rooms.add(new Room(nextRoomId++, "Day 8"));
        rooms.add(new Room(nextRoomId++, "Day 9"));
        rooms.add(new Room(nextRoomId++, "Day 10"));
        if (demoData == DemoData.LARGE) {
            rooms.add(new Room(nextRoomId++, "Room D"));
            rooms.add(new Room(nextRoomId++, "Room E"));
            rooms.add(new Room(nextRoomId++, "Room F"));
        }

        List<Lesson> lessons = new ArrayList<>();
        long nextLessonId = 0L;
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout1", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout1", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout1", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout1", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout1", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout2", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout2", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout2", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout2", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout2", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout3", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout3", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout3", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout3", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout3", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout4", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout4", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout4", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout4", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys", "Scout4", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout5", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout5", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout5", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout5", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout5", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout6", "Group C", Set.of("D2L",   "D5S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout6", "Group C", Set.of("D2L",   "D5S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout6", "Group C", Set.of("D2L",   "D5S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout6", "Group C", Set.of("D2L",   "D5S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout6", "Group C", Set.of("D2L",   "D5S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout7", "Group C", Set.of("D2L",   "D3S", "D4L", "D5S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout7", "Group C", Set.of("D2L",   "D3S", "D4L", "D5S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout7", "Group C", Set.of("D2L",   "D3S", "D4L", "D5S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout7", "Group C", Set.of("D2L",   "D3S", "D4L", "D5S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout7", "Group C", Set.of("D2L",   "D3S", "D4L", "D5S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout8", "Group C", Set.of("D2L",   "D5L", "D4S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout8", "Group C", Set.of("D2L",   "D5L", "D4S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout8", "Group C", Set.of("D2L",   "D5L", "D4S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout8", "Group C", Set.of("D2L",   "D5L", "D4S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",    "Scout8", "Group C", Set.of("D2L",   "D5L", "D4S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout9", "Group W", Set.of("Warrior",   "D5L", "D2S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout9", "Group W", Set.of("Warrior",   "D5L", "D2S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout9", "Group W", Set.of("Warrior",   "D5L", "D2S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout9", "Group W", Set.of("Warrior",   "D5L", "D2S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout9", "Group W", Set.of("Warrior",   "D5L", "D2S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout10", "Group C", Set.of("D3S", "D5S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout10", "Group C", Set.of("D3S", "D5S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout10", "Group C", Set.of("D3S", "D5S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout10", "Group C", Set.of("D3S", "D5S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout10", "Group C", Set.of("D3S", "D5S", "D6S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout11", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout11", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout11", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout11", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout11", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout12", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout12", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout12", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout12", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout12", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout13", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout13", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout13", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout13", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout13", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout14", "Group C", Set.of("D2L", "D2S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout14", "Group C", Set.of("D2L", "D2S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout14", "Group C", Set.of("D2L", "D2S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout14", "Group C", Set.of("D2L", "D2S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout14", "Group C", Set.of("D2L", "D2S")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout15", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout15", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout15", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout15", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Gorilla",    "Scout15", "Group C", Set.of("D2L", "D2S", "D6L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",    "Scout16", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",    "Scout16", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",    "Scout16", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",    "Scout16", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Monkeys",    "Scout16", "Grp D3L", Set.of("D3L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout17", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout17", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout17", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout17", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout18", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout18", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout18", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout18", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout18", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout18", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout19", "Group A", Set.of("D3L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout19", "Group A", Set.of("D3S",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout19", "Group A", Set.of("D3S",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout19", "Group A", Set.of("D3S",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout19", "Group A", Set.of("D3S",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout20", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout20", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout20", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout20", "Group A", Set.of("D2L",   "D2S", "D4L")));
        lessons.add(new Lesson(nextLessonId++, "Dogs",       "Scout20", "Group A", Set.of("D2L",   "D2S", "D4L")));
        if (demoData == DemoData.LARGE) {
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Scout1", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Scout1", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Math", "A. Scout1", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "ICT", "A. Scout1", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Physics", "M. Scout3", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Geography", "C. Scout5", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Geology", "C. Scout5", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "History", "I. Jones", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "English", "I. Jones", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Drama", "I. Jones", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Art", "S. Dali", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade",Set.of("D2S", "D3S", "D7L")));
            lessons.add(new Lesson(nextLessonId++, "Physical education", "C. Lewis", "9th grade",Set.of("D2S", "D3S", "D7L")));
        }

        
        return Response.ok(new Timetable(demoData.name(), timeslots, rooms, lessons)).build();
    }

}
