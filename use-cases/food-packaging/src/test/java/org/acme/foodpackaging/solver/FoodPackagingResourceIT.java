package org.acme.foodpackaging.solver;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
class FoodPackagingResourceIT {

    @Test
    void solveNative() {
        post("/schedule/solve")
                .then()
                .statusCode(204)
                .extract();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> !SolverStatus.NOT_SOLVING.name().equals(get("/schedule").jsonPath().get("solverStatus")));

        String score = get("/schedule").jsonPath().get("score");
        assertThat(score).isNotNull();
    }
}