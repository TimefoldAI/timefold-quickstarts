package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class VehicleRoutePlanOpenApiValidationTest {

    @Inject
    ObjectMapper mapper;

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstVisit(input).putNull("id");

        assertRejected(post(input), "visits[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("vehicles", mapper.createArrayNode());

        assertRejected(post(input), "modelInput.vehicles");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstVehicle(input).put("capacity", 0);

        assertRejected(post(input), "capacity");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstVisit(input).remove("demand");

        assertRejected(post(input), "demand");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstVisit(input).put("demand", "not-a-number");

        post(input).then().statusCode(400);
    }

    @Test
    void malformedDateTimeIsRejected() {
        ObjectNode input = demoData();
        firstVisit(input).put("minStartTime", "01/02/2024 08:00");

        assertRejected(post(input), "minStartTime");
    }

    @Test
    void offsetLessDateTimeIsRejected() {
        // The Service module validates a date-time against ISO-8601 *with* an offset, which is why
        // the whole model uses OffsetDateTime rather than LocalDateTime.
        ObjectNode input = demoData();
        firstVisit(input).put("minStartTime", "2024-01-01T08:00:00");

        assertRejected(post(input), "minStartTime");
    }

    private ObjectNode demoData() {
        String json = given().when().get("/v1/demo-data/BASIC").then().statusCode(200).extract().asString();
        try {
            return (ObjectNode) mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Demo data is not valid JSON.", e);
        }
    }

    private static ObjectNode modelInput(ObjectNode input) {
        return (ObjectNode) input.get("modelInput");
    }

    private static ObjectNode firstVehicle(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("vehicles").get(0);
    }

    private static ObjectNode firstVisit(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("visits").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/route-plans");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
