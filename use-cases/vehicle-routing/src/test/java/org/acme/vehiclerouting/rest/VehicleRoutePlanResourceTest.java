package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class VehicleRoutePlanResourceTest {

    @Test
    public void solveDemoDataUntilFeasible() {
        VehicleRoutePlan vehicleRoutePlan = given()
                .when().get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(vehicleRoutePlan)
                .expect().contentType(ContentType.TEXT)
                .when().post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/route-plans/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        VehicleRoutePlan solution = get("/route-plans/" + jobId).then().extract().as(VehicleRoutePlan.class);
        assertEquals(solution.getSolverStatus(), SolverStatus.NOT_SOLVING);
        assertNotNull(solution.getVehicles());
        assertNotNull(solution.getCustomers());
        assertNotNull(solution.getDepots());
        assertNotNull(solution.getVehicles().get(0).getCustomers());
        assertTrue(solution.getScore().isFeasible());
    }
}
