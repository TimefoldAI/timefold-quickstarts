package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.solver.SolverStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class VehicleRoutePlanResourceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeAll
    static void initializeJacksonParser() {
        // Registers required org.acme.vehiclerouting.domain.jackson.VRPScoreAnalysisJacksonModule,
        // see META-INF/services/com.fasterxml.jackson.databind.Module.
        OBJECT_MAPPER.findAndRegisterModules();
    }

    @Test
    public void solveDemoDataUntilFeasible() {
        VehicleRoutePlan solution = solveDemoData();
        assertTrue(solution.getScore().isFeasible());
    }

    @Test
    public void analyzeFetchAll() throws JsonProcessingException {
        VehicleRoutePlan solution = solveDemoData();
        assertTrue(solution.getScore().isFeasible());

        String analysisAsString = given()
                .contentType(ContentType.JSON)
                .body(solution)
                .expect().contentType(ContentType.JSON)
                .when()
                .put("/route-plans/analyze")
                .then()
                .extract()
                .asString();

        ScoreAnalysis<?> analysis = parseScoreAnalysis(analysisAsString);

        assertNotNull(analysis.score());
        ConstraintAnalysis<?> minimizeTravelTimeAnalysis =
                analysis.getConstraintAnalysis(VehicleRoutePlan.class.getPackageName(), "minimizeTravelTime");
        assertNotNull(minimizeTravelTimeAnalysis);
        assertNotNull(minimizeTravelTimeAnalysis.matches());
        assertFalse(minimizeTravelTimeAnalysis.matches().isEmpty());
    }

    @Test
    public void analyzeFetchShallow() throws JsonProcessingException {
        VehicleRoutePlan solution = solveDemoData();
        assertTrue(solution.getScore().isFeasible());

        String analysisAsString = given()
                .contentType(ContentType.JSON)
                .queryParam("fetchPolicy", "FETCH_SHALLOW")
                .body(solution)
                .expect().contentType(ContentType.JSON)
                .when()
                .put("/route-plans/analyze")
                .then()
                .extract()
                .asString();

        ScoreAnalysis<?> analysis = parseScoreAnalysis(analysisAsString);

        assertNotNull(analysis.score());
        ConstraintAnalysis<?> minimizeTravelTimeAnalysis =
                analysis.getConstraintAnalysis(VehicleRoutePlan.class.getPackageName(), "minimizeTravelTime");
        assertNotNull(minimizeTravelTimeAnalysis);
        assertNull(minimizeTravelTimeAnalysis.matches());
    }

    private VehicleRoutePlan solveDemoData() {
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
        assertNotNull(solution.getVisits());
        assertNotNull(solution.getDepots());
        assertNotNull(solution.getVehicles().get(0).getVisits());
        return solution;
    }

    private ScoreAnalysis<?> parseScoreAnalysis(String analysis) throws JsonProcessingException {
        assertNotNull(analysis);
        return OBJECT_MAPPER.readValue(analysis, ScoreAnalysis.class);
    }
}
