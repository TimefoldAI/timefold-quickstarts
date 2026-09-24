package org.acme.meetingschedule.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class MeetingScheduleOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstPerson(input).putNull("id");

        assertRejected(post(input), "people[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("meetings", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.meetings");
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

    @Test
    void offsetLessDateTimeIsRejected() {
        ObjectNode input = demoData();
        firstOfficeDay(input).put("startDateTime", "2024-01-01T08:00:00");

        assertRejected(post(input), "startDateTime");
    }

    @Test
    void belowMinimumGranularityIsRejected() {
        ObjectNode input = demoData();
        timeConfiguration(input).put("granularityInMinutes", 0);

        assertRejected(post(input), "granularityInMinutes");
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

    private static ObjectNode firstPerson(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("people").get(0);
    }

    private static ObjectNode firstRoom(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("rooms").get(0);
    }

    private static ObjectNode timeConfiguration(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("timeConfiguration");
    }

    private static ObjectNode firstOfficeDay(ObjectNode input) {
        return (ObjectNode) timeConfiguration(input).get("days").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
