package org.acme.tournamentschedule.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class TournamentScheduleOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstTeam(input).putNull("id");

        assertRejected(post(input), "teams[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("matches", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.matches");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstMatch(input).remove("date");

        assertRejected(post(input), "date");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstMatch(input).put("date", 12345);

        post(input).then().statusCode(400);
    }

    @Test
    void malformedDateIsRejected() {
        ObjectNode input = demoData();
        firstMatch(input).put("date", "not-a-date");

        assertRejected(post(input), "date");
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

    private static ObjectNode firstTeam(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("teams").get(0);
    }

    private static ObjectNode firstMatch(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("matches").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
