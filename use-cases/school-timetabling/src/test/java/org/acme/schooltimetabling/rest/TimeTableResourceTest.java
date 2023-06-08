package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.core.api.solver.SolverStatus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class TimeTableResourceTest {

    @Test
    public void solveDemoDataUntilFeasible() {
        TimeTable testTimeTable = createTestTimeTable();

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(testTimeTable)
                .expect().contentType(ContentType.TEXT)
                .when().post("/timetables")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollDelay(Duration.ofSeconds(5))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(get("/timetables/"+ jobId + "?retrieve=STATUS")
                        .body()
                        .as(TimeTable.class).getSolverStatus()));

        get("/timetables").then().assertThat()
                .body("solverStatus", equalTo(SolverStatus.NOT_SOLVING.name()))
                .body("timeslotList", is(not(empty())))
                .body("roomList", is(not(empty())))
                .body("lessonList", is(not(empty())))
                .body("lessonList.timeslot", not(nullValue()))
                .body("lessonList.room", not(nullValue()));
    }

    private static TimeTable createTestTimeTable() {
        List<Timeslot> timeslotList = new ArrayList<>(10);
        long nextTimeslotId = 0L;
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(nextTimeslotId++, DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        long nextRoomId = 0L;
        List<Room> roomList = List.of(new Room(nextRoomId++, "Room A"), new Room(nextRoomId++, "Room B"));

        List<Lesson> lessonList = new ArrayList<>();
        long nextLessonId = 0L;
        lessonList.add(new Lesson(nextLessonId++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Physics", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(nextLessonId++, "Geography", "C. Darwin", "10th grade"));

        return new TimeTable("test", timeslotList, roomList, lessonList);
    }
}