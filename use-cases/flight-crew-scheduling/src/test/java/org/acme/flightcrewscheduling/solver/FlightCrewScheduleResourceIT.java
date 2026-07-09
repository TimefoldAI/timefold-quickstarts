package org.acme.flightcrewscheduling.solver;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;

@QuarkusIntegrationTest
class FlightCrewScheduleResourceIT {

    private static final Set<String> TERMINAL_STATUSES = Set.of(
            "DATASET_INVALID",
            "SOLVING_COMPLETED",
            "SOLVING_FAILED",
            "SOLVING_INCOMPLETE");

    @Test
    void solveNative() {
        String demoDataJson = given().when().get("/v1/demo-data/BASIC").then().statusCode(200).extract().asString();

        String datasetId = given().contentType(ContentType.JSON).body(demoDataJson).when()
                .post("/v1/schedules").then().statusCode(202).extract().jsonPath().getString("id");

        assertThat(datasetId).isNotNull();

        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500L)).until(() -> TERMINAL_STATUSES
                .contains(get("/v1/schedules/" + datasetId).jsonPath().getString("metadata.solverStatus")));

        var response = get("/v1/schedules/" + datasetId).then().extract();
        assertThat(response.jsonPath().getString("metadata.solverStatus")).isEqualTo("SOLVING_COMPLETED");
        var score = response.jsonPath().getString("metadata.score");
        assertThat(score).isNotNull();
    }
}
