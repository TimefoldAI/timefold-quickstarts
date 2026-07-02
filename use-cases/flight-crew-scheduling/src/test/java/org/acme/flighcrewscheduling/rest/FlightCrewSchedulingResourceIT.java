package org.acme.flighcrewscheduling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;

@QuarkusIntegrationTest
class FlightCrewSchedulingResourceIT {

    @Test
    void solveNative() {
        FlightCrewSchedule schedule = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(FlightCrewSchedule.class);

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

        FlightCrewSchedule solution = get("/schedules/" + jobId).then().extract().as(FlightCrewSchedule.class);
        assertThat(solution).isNotNull();
    }
}