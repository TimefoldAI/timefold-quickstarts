package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.dto.ApplyRecommendationRequest;
import org.acme.vehiclerouting.domain.dto.RecommendationRequest;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
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

    @Test
    public void testRecommendedFit() throws JsonProcessingException {
        VehicleRoutePlan vehicleRoutePlan = given()
                .when().get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        int size = vehicleRoutePlan.getCustomers().size();
        Customer newCustomer = vehicleRoutePlan.getCustomers().get(size - 1);
        vehicleRoutePlan.getCustomers().remove(size - 1);

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
        assertNotNull(solution);
        assertEquals(solution.getSolverStatus(), SolverStatus.NOT_SOLVING);
        assertEquals(size - 1, solution.getCustomers().size());

        // Request recommendation
        solution.getCustomers().add(newCustomer);
        RecommendationRequest request = new RecommendationRequest(solution, newCustomer);
        List<Pair<VehicleRecommendation, ScoreAnalysis>> recommendedFitList = parseRecommendedFitList(given()
                .contentType(ContentType.JSON)
                .body(request)
                .expect().contentType(ContentType.JSON)
                .when()
                .post("/route-plans/recommendation")
                .then()
                .extract()
                .as(List.class));

        assertNotNull(recommendedFitList);
        assertEquals(5, recommendedFitList.size());

        // Apply the recommendation
        VehicleRecommendation recommendation = recommendedFitList.get(0).getLeft();
        ApplyRecommendationRequest applyRequest = new ApplyRecommendationRequest(solution, newCustomer,
                recommendation.vehicle().getId(), recommendation.index());

        VehicleRoutePlan updatedSolution = given()
                .contentType(ContentType.JSON)
                .body(applyRequest)
                .expect().contentType(ContentType.JSON)
                .when()
                .post("/route-plans/recommendation/apply")
                .then()
                .extract()
                .as(VehicleRoutePlan.class);

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
        assertNotNull(solution.getCustomers());
        assertNotNull(solution.getDepots());
        assertNotNull(solution.getVehicles().get(0).getCustomers());
        return solution;
    }

    private ScoreAnalysis<?> parseScoreAnalysis(String analysis) throws JsonProcessingException {
        assertNotNull(analysis);
        return OBJECT_MAPPER.readValue(analysis, ScoreAnalysis.class);
    }

    private List<Pair<VehicleRecommendation, ScoreAnalysis>> parseRecommendedFitList(List<Map<String, Object>> recommendedFitMap) {
        assertNotNull(recommendedFitMap);
        List<Pair<VehicleRecommendation, ScoreAnalysis>> recommendedFitList = new ArrayList<>(recommendedFitMap.size());
        recommendedFitMap.forEach(record -> recommendedFitList.add(Pair.of(
                OBJECT_MAPPER.convertValue(record.get("proposition"), VehicleRecommendation.class),
                OBJECT_MAPPER.convertValue(record.get("scoreDiff"), ScoreAnalysis.class)
        )));
        return recommendedFitList;
    }
}
