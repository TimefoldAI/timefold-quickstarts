package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class TimetableResourceTest {

    @Test
    void solveDemoDataUntilFeasible() {
        Timetable testTimetable = given()
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(Timetable.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(testTimetable)
                .expect().contentType(ContentType.TEXT)
                .when().post("/timetables")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/timetables/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        Timetable solution = get("/timetables/" + jobId).then().extract().as(Timetable.class);
        assertEquals(SolverStatus.NOT_SOLVING, solution.getSolverStatus());
        assertNotNull(solution.getLessons());
        assertNotNull(solution.getTimeslots());
        assertNotNull(solution.getRooms());
        assertNotNull(solution.getLessons().get(0).getRoom());
        assertNotNull(solution.getLessons().get(0).getTimeslot());
        assertTrue(solution.getScore().isFeasible());
    }

    @Test
    void analyze() {
        Timetable testTimetable = given()
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(Timetable.class);
        var rooms = testTimetable.getRooms();
        var timeslots = testTimetable.getTimeslots();
        int i = 0;
        for (var lesson : testTimetable.getLessons()) { // Initialize the solution.
            lesson.setRoom(rooms.get(i % rooms.size()));
            lesson.setTimeslot(timeslots.get(i % timeslots.size()));
            i += 1;
        }

        String analysis = given()
                .contentType(ContentType.JSON)
                .body(testTimetable)
                .expect().contentType(ContentType.JSON)
                .when()
                .put("/timetables/analyze")
                .then()
                .extract()
                .asString();
        assertNotNull(analysis); // Too long to validate in its entirety.

        String analysis2 = given()
                .contentType(ContentType.JSON)
                .queryParam("fetchPolicy", "FETCH_SHALLOW")
                .body(testTimetable)
                .expect().contentType(ContentType.JSON)
                .when()
                .put("/timetables/analyze")
                .then()
                .extract()
                .asString();
        assertNotNull(analysis2); // Too long to validate in its entirety.
    }

}