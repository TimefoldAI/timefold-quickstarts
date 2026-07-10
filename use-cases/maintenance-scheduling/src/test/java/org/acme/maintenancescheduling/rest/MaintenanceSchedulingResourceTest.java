package org.acme.maintenancescheduling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class MaintenanceSchedulingResourceTest {

    @Test
    void solveDemoDataUntilFeasible() {
        MaintenanceSchedule maintenanceSchedule = given()
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(MaintenanceSchedule.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(maintenanceSchedule)
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

        MaintenanceSchedule solution = get("/schedules/" + jobId).then().extract().as(MaintenanceSchedule.class);
        assertEquals(SolverStatus.NOT_SOLVING, solution.getSolverStatus());
        assertFalse(solution.getJobs().isEmpty());
        for (Job job : solution.getJobs()) {
            assertNotNull(job.getCrew());
            assertNotNull(job.getStartDate());
        }
        assertTrue(solution.getScore().isFeasible());
    }
}
