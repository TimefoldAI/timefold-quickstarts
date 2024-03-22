package org.acme.bedallocation.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.bedallocation.domain.Schedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class BedSchedulingResourceTest {

    @Test
    void solveDemoDataUntilFeasible() {
        Schedule schedule = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(Schedule.class);

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

        Schedule solution = get("/schedules/" + jobId).then().extract().as(Schedule.class);
        assertThat(solution.getSolverStatus()).isEqualTo(SolverStatus.NOT_SOLVING);
        assertThat(solution.getBedDesignations().stream().allMatch(bedDesignation -> bedDesignation.getBed() != null)).isTrue();
        assertThat(solution.getScore().isFeasible()).isTrue();
    }

    @Test
    void analyze() {
        Schedule schedule = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(Schedule.class);

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

        Schedule solution = get("/schedules/" + jobId).then().extract().as(Schedule.class);

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