package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

@QuarkusTest
public class SolverResourceTest {

    @Test
    public void solve() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .post("/vrp/solve")
                .then()
                .statusCode(204);

        await().until(() -> !given()
                .when()
                .contentType(ContentType.JSON)
                .get("/vrp/status")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getBoolean("isSolving"));

        String scoreString = given()
                .when()
                .contentType(ContentType.JSON)
                .get("/vrp/status")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().get("solution.score");
        assertTrue(HardSoftScore.parseScore(scoreString).isFeasible());
    }
}
