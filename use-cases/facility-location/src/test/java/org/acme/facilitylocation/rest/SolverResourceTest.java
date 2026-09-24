package org.acme.facilitylocation.rest;

import static io.restassured.RestAssured.get;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverResourceTest {

    @Test
    void swaggerUiAvailable() {
        get("/q/swagger-ui").then().statusCode(200);
    }

    @Test
    void openApiAvailable() {
        get("/q/openapi").then().statusCode(200);
    }
}
