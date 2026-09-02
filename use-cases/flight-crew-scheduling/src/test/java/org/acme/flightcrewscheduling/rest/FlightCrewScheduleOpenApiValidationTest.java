package org.acme.flightcrewscheduling.rest;

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
class FlightCrewScheduleOpenApiValidationTest {

    @Inject
    ObjectMapper mapper;

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstAirport(input).putNull("code");

        assertRejected(post(input), "airports[0].code");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("flights", mapper.createArrayNode());

        assertRejected(post(input), "modelInput.flights");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstFlightAssignment(input).put("indexInFlight", 0);

        assertRejected(post(input), "indexInFlight");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstFlightAssignment(input).remove("requiredSkill");

        assertRejected(post(input), "requiredSkill");
    }

    @Test
    void offsetLessDateTimeIsRejected() {
        ObjectNode input = demoData();
        firstFlight(input).put("departureUTCDateTime", "2024-01-01T06:00:00");

        assertRejected(post(input), "departureUTCDateTime");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstFlightAssignment(input).put("indexInFlight", "not-a-number");

        post(input).then().statusCode(400);
    }

    private ObjectNode demoData() {
        String json = given().when().get("/v1/demo-data/BASIC").then().statusCode(200).extract().asString();
        try {
            return (ObjectNode) mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Demo data is not valid JSON.", e);
        }
    }

    private ObjectNode modelInput(ObjectNode input) {
        return (ObjectNode) input.get("modelInput");
    }

    private ObjectNode firstAirport(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("airports").get(0);
    }

    private ObjectNode firstFlight(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("flights").get(0);
    }

    private ObjectNode firstFlightAssignment(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("flightAssignments").get(0);
    }

    private Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
