package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class TimetableOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        Response response = post(demoData());
        assertThat(response.getStatusCode()).as(response.getBody().asString()).isEqualTo(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstLesson(input).putNull("teacher");

        assertRejected(post(input), "lessons[0].teacher");
    }

    @Test
    void emptyRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstLesson(input).put("studentGroup", "");

        assertRejected(post(input), "studentGroup");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("rooms", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.rooms");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstTimeslot(input).remove("startTime");

        assertRejected(post(input), "startTime");
    }

    @Test
    void unknownDayOfWeekIsRejected() {
        ObjectNode input = demoData();
        firstTimeslot(input).put("dayOfWeek", "FUNDAY");

        assertRejected(post(input), "dayOfWeek");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstTimeslot(input).put("startTime", 830);

        post(input).then().statusCode(400);
    }

    private static ObjectNode demoData() {
        String json = given().when().get("/v1/demo-data/SMALL").then().statusCode(200).extract().asString();
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

    private static ObjectNode firstLesson(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("lessons").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/timetables");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
