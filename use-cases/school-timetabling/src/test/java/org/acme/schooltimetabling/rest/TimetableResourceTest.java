package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import jakarta.inject.Singleton;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintRef;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.core.api.solver.SolverStatus;
import ai.timefold.solver.jackson.api.score.analysis.AbstractScoreAnalysisJacksonDeserializer;

import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class TimetableResourceTest {

    @Test
    public void solveDemoDataUntilFeasible() {
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
        assertEquals(solution.getSolverStatus(), SolverStatus.NOT_SOLVING);
        assertNotNull(solution.getLessons());
        assertNotNull(solution.getTimeslots());
        assertNotNull(solution.getRooms());
        assertNotNull(solution.getLessons().get(0).getRoom());
        assertNotNull(solution.getLessons().get(0).getTimeslot());
        assertTrue(solution.getScore().isFeasible());
    }

    @Test
    public void analyze() {
        Timetable testTimetable = given()
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(Timetable.class);
        var roomList = testTimetable.getRooms();
        var timeslotList = testTimetable.getTimeslots();
        int i = 0;
        for (var lesson : testTimetable.getLessons()) { // Initialize the solution.
            lesson.setRoom(roomList.get(i % roomList.size()));
            lesson.setTimeslot(timeslotList.get(i % timeslotList.size()));
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