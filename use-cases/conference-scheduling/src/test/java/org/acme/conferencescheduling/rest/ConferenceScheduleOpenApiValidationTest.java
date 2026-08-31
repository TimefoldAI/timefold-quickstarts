package org.acme.conferencescheduling.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class ConferenceScheduleOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstTimeslot(input).putNull("id");

        assertRejected(post(input), "timeslots[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("talks", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.talks");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstRoom(input).put("capacity", 0);

        assertRejected(post(input), "capacity");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstRoom(input).remove("capacity");

        assertRejected(post(input), "capacity");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstRoom(input).put("capacity", "not-a-number");

        post(input).then().statusCode(400);
    }

    private static ObjectNode demoData() {
        String json = given().when().get("/v1/demo-data/BASIC").then().statusCode(200).extract().asString();
        try {
            return (ObjectNode) MAPPER.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Demo data is not valid JSON.", e);
        }
    }

    private static ObjectNode modelInput(ObjectNode input) {
        return (ObjectNode) input.get("modelInput");
    }

    private static ObjectNode firstTimeslot(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("timeslots").get(0);
    }

    private static ObjectNode firstRoom(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("rooms").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
