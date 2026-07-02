package org.acme.orderpicking.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
class OrderPickingResourceIT {

    @Test
    void solveNative() {
        post("/orderPicking/solve")
                .then()
                .statusCode(204)
                .extract();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> !SolverStatus.NOT_SOLVING.name().equals(get("/orderPicking").jsonPath().get("solverStatus")));

        var score = get("/orderPicking").jsonPath().getString("solution.score");
        assertThat(score).isNotNull();
    }
}