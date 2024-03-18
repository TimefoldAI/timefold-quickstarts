package org.acme.conferencescheduling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ConferenceScheduleResourceTest {

    @Test
    void solveDemoDataUntilFeasible() {
        ConferenceSchedule schedule = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(ConferenceSchedule.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(schedule)
                .expect().contentType(ContentType.TEXT)
                .when().post("/schedules")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/schedules/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        ConferenceSchedule solution = get("/schedules/" + jobId).then().extract().as(ConferenceSchedule.class);
        assertThat(solution.getSolverStatus()).isEqualTo(SolverStatus.NOT_SOLVING);
        assertThat(solution.getTalks().stream().allMatch(t -> t.getTimeslot() != null && t.getRoom() != null)).isTrue();
        assertThat(solution.getScore().isFeasible()).isTrue();
    }

    @Test
    void analyze() {
        ConferenceSchedule schedule = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(ConferenceSchedule.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(schedule)
                .expect().contentType(ContentType.TEXT)
                .when().post("/schedules")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/schedules/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        ConferenceSchedule solution = get("/schedules/" + jobId).then().extract().as(ConferenceSchedule.class);

        String analysis = given()
                .contentType(ContentType.JSON)
                .body(solution)
                .expect().contentType(ContentType.JSON)
                .when()
                .put("/schedules/analyze")
                .then()
                .extract()
                .asString();
        // There are too many constraints to validate
        assertThat(analysis).isNotNull();

        String analysis2 = given()
                .contentType(ContentType.JSON)
                .queryParam("fetchPolicy", "FETCH_SHALLOW")
                .body(solution)
                .expect().contentType(ContentType.JSON)
                .when()
                .put("/schedules/analyze")
                .then()
                .extract()
                .asString();
        // There are too many constraints to validate
        assertThat(analysis2).isNotNull();
    }

}