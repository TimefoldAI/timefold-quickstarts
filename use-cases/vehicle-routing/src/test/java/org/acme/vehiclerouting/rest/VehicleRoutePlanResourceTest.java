package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.dto.ApplyRecommendationRequest;
import org.acme.vehiclerouting.domain.dto.RecommendationRequest;
import org.acme.vehiclerouting.domain.dto.VehicleRecommendation;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private VehicleRoutePlan generateInitialSolution() {
        // Fetching the problem data
        VehicleRoutePlan vehicleRoutePlan = given()
                .when().get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        // Starting the optimization
        String jobId = given()
                .contentType(ContentType.JSON)
                .body(vehicleRoutePlan)
                .expect().contentType(ContentType.TEXT)
                .when().post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Waiting for the solution
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/route-plans/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        VehicleRoutePlan solution = get("/route-plans/" + jobId).then().extract().as(VehicleRoutePlan.class);
        return solution;
    }

    private Visit generateNewVisit(VehicleRoutePlan solution) {
        Visit newVisit = new Visit(String.valueOf(solution.getVisits().size() + 1),
                "visit%d".formatted(solution.getVisits().size() + 1), new Location(43.77800837529796, 11.223969038020176),
                2, LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                Duration.ofMinutes(10));
        solution.getVisits().add(newVisit);
        return newVisit;
    }

    private List<Pair<VehicleRecommendation, ScoreAnalysis>> getRecommendations(VehicleRoutePlan solution, Visit newVisit) {
        RecommendationRequest request = new RecommendationRequest(solution, newVisit.getId());
        return parseRecommendedFitList(given()
                .contentType(ContentType.JSON)
                .body(request)
                .expect().contentType(ContentType.JSON)
                .when()
                .post("/route-plans/recommendation")
                .then()
                .extract()
                .as(List.class));
    }

    private VehicleRoutePlan applyBestRecommendation(VehicleRoutePlan solution, Visit newVisit,
            List<Pair<VehicleRecommendation, ScoreAnalysis>> recommendedFitList) {
        // Selects the best recommendation
        VehicleRecommendation recommendation = recommendedFitList.get(0).getLeft();
        ApplyRecommendationRequest applyRequest = new ApplyRecommendationRequest(solution, newVisit.getId(),
                recommendation.vehicleId(), recommendation.index());

        // Applies the recommendation
        return given()
                .contentType(ContentType.JSON)
                .body(applyRequest)
                .expect().contentType(ContentType.JSON)
                .when()
                .post("/route-plans/recommendation/apply")
                .then()
                .extract()
                .as(VehicleRoutePlan.class);
    }

    @Test
    public void testRecommendedFit() {
        // Generate an initial solution
        VehicleRoutePlan solution = generateInitialSolution();
        assertNotNull(solution);
        assertEquals(solution.getSolverStatus(), SolverStatus.NOT_SOLVING);

        // Create a new visit
        Visit newVisit = generateNewVisit(solution);

        // Request recommendation
        List<Pair<VehicleRecommendation, ScoreAnalysis>> recommendedFitList = getRecommendations(solution, newVisit);
        assertNotNull(recommendedFitList);
        assertEquals(5, recommendedFitList.size());

        // Apply the best recommendation
        VehicleRoutePlan updatedSolution = applyBestRecommendation(solution, newVisit, recommendedFitList);
        assertNotNull(updatedSolution);
        assertNotEquals(updatedSolution.getScore().toString(), solution.getScore().toString());
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
        assertNotNull(solution.getVehicles().get(0).getVisits());
        return solution;
    }

    private ScoreAnalysis<?> parseScoreAnalysis(String analysis) throws JsonProcessingException {
        assertNotNull(analysis);
        return OBJECT_MAPPER.readValue(analysis, ScoreAnalysis.class);
    }

    private List<Pair<VehicleRecommendation, ScoreAnalysis>>
            parseRecommendedFitList(List<Map<String, Object>> recommendedFitMap) {
        assertNotNull(recommendedFitMap);
        List<Pair<VehicleRecommendation, ScoreAnalysis>> recommendedFitList = new ArrayList<>(recommendedFitMap.size());
        recommendedFitMap.forEach(record -> recommendedFitList.add(Pair.of(
                OBJECT_MAPPER.convertValue(record.get("proposition"), VehicleRecommendation.class),
                OBJECT_MAPPER.convertValue(record.get("scoreDiff"), ScoreAnalysis.class))));
        return recommendedFitList;
    }
}
