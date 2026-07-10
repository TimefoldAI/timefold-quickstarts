package org.acme.kotlin.schooltimetabling.rest

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.acme.kotlin.schooltimetabling.domain.Lesson
import org.acme.kotlin.schooltimetabling.domain.Room
import org.acme.kotlin.schooltimetabling.domain.Timeslot
import org.acme.kotlin.schooltimetabling.domain.Timetable
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalTime

@Tag(name = "Demo data", description = "Timefold-provided demo school timetable data.")
@Path("demo-data")
class TimetableDemoResource {

    enum class DemoData {
        SMALL, LARGE
    }

    @APIResponses(
        value = [APIResponse(
            responseCode = "200", description = "List of demo data represented as IDs.", content = [Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = DemoData::class, type = SchemaType.ARRAY)
            )]
        )]
    )
    @Operation(summary = "List demo data.")
    @GET
    fun list(): Array<DemoData> {
        return DemoData.entries.toTypedArray()
    }

    @APIResponses(
        value = [APIResponse(
            responseCode = "200", description = "Unsolved demo timetable.", content = [Content(
                mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Timetable::class)
            )]
        )]
    )
    @Operation(summary = "Find an unsolved demo timetable by ID.")
    @GET
    @Path("/{demoDataId}")
    fun generate(
        @Parameter(
            description = "Unique identifier of the demo data.", required = true
        ) @PathParam("demoDataId") demoData: DemoData
    ): Response {
        val timeslots: MutableList<Timeslot> = ArrayList(10)
        var nextTimeslotId = 0L
        timeslots.add(Timeslot(nextTimeslotId++.toString(), MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
        timeslots.add(Timeslot(nextTimeslotId++.toString(), TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
        if (demoData == DemoData.LARGE) {
            timeslots.add(Timeslot(nextTimeslotId++.toString(), WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
            timeslots.add(Timeslot(nextTimeslotId++.toString(), FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
            timeslots.add(Timeslot(nextTimeslotId.toString(), FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
        }
        val rooms: MutableList<Room> = ArrayList<Room>(3)
        var nextRoomId = 0L
        rooms.add(Room(nextRoomId++.toString(), "Room A"))
        rooms.add(Room(nextRoomId++.toString(), "Room B"))
        rooms.add(Room(nextRoomId++.toString(), "Room C"))
        if (demoData == DemoData.LARGE) {
            rooms.add(Room(nextRoomId++.toString(), "Room D"))
            rooms.add(Room(nextRoomId++.toString(), "Room E"))
            rooms.add(Room(nextRoomId.toString(), "Room F"))
        }
        val lessons: MutableList<Lesson> = ArrayList<Lesson>()
        var nextLessonId = 0L
        lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Chemistry", "M. Curie", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Biology", "C. Darwin", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "English", "I. Jones", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "English", "I. Jones", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Spanish", "P. Cruz", "9th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Spanish", "P. Cruz", "9th grade"))
        if (demoData == DemoData.LARGE) {
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "ICT", "A. Turing", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geography", "C. Darwin", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geology", "C. Darwin", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "I. Jones", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Drama", "I. Jones", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "9th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "9th grade"))
        }
        lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Chemistry", "M. Curie", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "French", "M. Curie", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Geography", "C. Darwin", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "10th grade"))
        lessons.add(Lesson(nextLessonId++.toString(), "Spanish", "P. Cruz", "10th grade"))
        if (demoData == DemoData.LARGE) {
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "ICT", "A. Turing", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Biology", "C. Darwin", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geology", "C. Darwin", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Drama", "I. Jones", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "10th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "ICT", "A. Turing", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Chemistry", "M. Curie", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "French", "M. Curie", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geography", "C. Darwin", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Biology", "C. Darwin", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geology", "C. Darwin", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Spanish", "P. Cruz", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Drama", "P. Cruz", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "11th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Math", "A. Turing", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "ICT", "A. Turing", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Chemistry", "M. Curie", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "French", "M. Curie", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physics", "M. Curie", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geography", "C. Darwin", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Biology", "C. Darwin", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Geology", "C. Darwin", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "History", "I. Jones", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "English", "P. Cruz", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Spanish", "P. Cruz", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Drama", "P. Cruz", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Art", "S. Dali", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "12th grade"))
            lessons.add(Lesson(nextLessonId++.toString(), "Physical education", "C. Lewis", "12th grade"))
            lessons.add(Lesson(nextLessonId.toString(), "Physical education", "C. Lewis", "12th grade"))
        }
        return Response.ok(Timetable(demoData.name, timeslots, rooms, lessons))
            .build()
    }
}