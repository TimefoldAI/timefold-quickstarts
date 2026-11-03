package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.acme.vehiclerouting.support.TestHelper.aVehicleDTO;
import static org.acme.vehiclerouting.support.TestHelper.aVisitDTO;
import static org.acme.vehiclerouting.support.TestHelper.input;
import static org.acme.vehiclerouting.support.TestHelper.recommendationRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;

import org.acme.vehiclerouting.dto.input.RecommendationRequestInput;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * The recommendation endpoint lives next to the ones the Service module generates under the same
 * {@code /route-plans} path. As this is an enterprise only feature, these tests only activate if enterprise is loaded.
 */
@QuarkusTest
@EnabledIfSystemProperty(named = "timefold.solver.enterprise", matches = "true")
class VehicleRoutePlanRecommendationResourceTest {

    private static final String RECOMMENDATION_PATH = "/v1/route-plans/recommendation";

    @Inject
    ObjectMapper mapper;

    /**
     * One vehicle already driving two visits, plus a third visit that is on no route yet: the shape
     * the endpoint expects.
     */
    private static VehicleRoutePlanInput planWithOneUnassignedVisit() {
        return input(
                List.of(aVehicleDTO("1").capacity(20).location(51.00, 3.65).visitIds(List.of("1", "2")).build()),
                List.of(aVisitDTO("1").location(51.01, 3.66).build(),
                        aVisitDTO("2").location(51.02, 3.68).build(),
                        aVisitDTO("3").location(51.03, 3.70).build()));
    }

    @Test
    void assignedVisitIsRejected() {
        Response response = post(recommendationRequest(planWithOneUnassignedVisit(), "1"));

        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains("already assigned");
    }

    @Test
    void unknownVisitIsRejected() {
        Response response = post(recommendationRequest(planWithOneUnassignedVisit(), "does-not-exist"));

        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains("does-not-exist");
    }

    private Response post(RecommendationRequestInput request) {
        try {
            return given().contentType(ContentType.JSON).body(mapper.writeValueAsString(request))
                    .when().post(RECOMMENDATION_PATH);
        } catch (Exception e) {
            throw new IllegalStateException("The request is not serializable.", e);
        }
    }
}
