package org.acme.facilitylocation.solver;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
class FacilityLocationResourceIT {

    @Test
    void solveNative() {
        post("/flp/solve")
                .then()
                .statusCode(204)
                .extract();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> !get("/flp/status").jsonPath().getBoolean("isSolving"));

        String score = get("/flp/status").jsonPath().get("solution.score");
        assertThat(score).isNotNull();
    }
}