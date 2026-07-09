package org.acme.meetingschedule.solver;

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
class MeetingScheduleResourceIT {

    private static final Set<String> TERMINAL_STATUSES = Set.of(
            "DATASET_INVALID",
            "SOLVING_COMPLETED",
            "SOLVING_FAILED",
            "SOLVING_INCOMPLETE");

    @Test
    void solveNative() {
        String demoDataJson = given().when().get("/v1/demo-data/BASIC").then().statusCode(200).extract().asString();

        String datasetId = given().contentType(ContentType.JSON).body(demoDataJson).when()
                .post("/v1/meeting-schedules").then().statusCode(202).extract().jsonPath().getString("id");

        assertThat(datasetId).isNotNull();

        await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofMillis(500L)).until(() -> {
            String status = get("/v1/meeting-schedules/" + datasetId).jsonPath().getString("metadata.solverStatus");
            return status != null && TERMINAL_STATUSES.contains(status);
        });

        var response = get("/v1/meeting-schedules/" + datasetId).then().extract();
        assertThat(response.jsonPath().getString("metadata.solverStatus")).isEqualTo("SOLVING_COMPLETED");
        var score = response.jsonPath().getString("metadata.score");
        assertThat(score).isNotNull();
    }
}
