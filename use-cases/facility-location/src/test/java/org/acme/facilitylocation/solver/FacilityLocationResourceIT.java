package org.acme.facilitylocation.solver;

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
class FacilityLocationResourceIT {

    private static final Set<String> TERMINAL_STATUSES = Set.of(
            "DATASET_INVALID",
            "SOLVING_COMPLETED",
            "SOLVING_FAILED",
            "SOLVING_INCOMPLETE");

    @Test
    void solveNative() {
        // 1. Get demo data (returns ModelRequest JSON)
        String demoDataJson = given().when().get("/v1/demo-data/BASIC").then().statusCode(200).extract().asString();

        // 2. Submit for solving (POST returns 202 + flat Metadata with top-level "id")
        String datasetId = given().contentType(ContentType.JSON).body(demoDataJson).when()
                .post("/v1/facilitylocations").then().statusCode(202).extract().jsonPath().getString("id");

        assertThat(datasetId).isNotNull();

        // 3. Poll for a terminal status so invalid datasets do not look like a hang
        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(500L)).until(() -> TERMINAL_STATUSES
                .contains(get("/v1/facilitylocations/" + datasetId).jsonPath().getString("metadata.solverStatus")));

        // 4. Verify solution
        var response = get("/v1/facilitylocations/" + datasetId).then().extract();
        assertThat(response.jsonPath().getString("metadata.solverStatus")).isEqualTo("SOLVING_COMPLETED");
        var score = response.jsonPath().getString("metadata.score");
        assertThat(score).isNotNull();
    }
}
