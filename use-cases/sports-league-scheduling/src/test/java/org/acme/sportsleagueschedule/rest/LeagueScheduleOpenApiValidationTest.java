package org.acme.sportsleagueschedule.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class LeagueScheduleOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstMatch(input).putNull("homeTeamId");

        assertRejected(post(input), "matches[0].homeTeamId");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("rounds", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.rounds");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstRound(input).put("index", -1);

        assertRejected(post(input), "index");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstMatch(input).remove("classicMatch");

        assertRejected(post(input), "classicMatch");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstRound(input).put("index", "not-a-number");

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

    private static ObjectNode firstMatch(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("matches").get(0);
    }

    private static ObjectNode firstRound(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("rounds").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
