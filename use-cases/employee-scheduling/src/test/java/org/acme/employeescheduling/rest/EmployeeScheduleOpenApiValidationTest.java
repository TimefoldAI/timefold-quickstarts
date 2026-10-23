package org.acme.employeescheduling.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class EmployeeScheduleOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        Response response = post(demoData());
        assertThat(response.getStatusCode()).as(response.getBody().asString()).isEqualTo(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstShift(input).putNull("location");

        assertRejected(post(input), "shifts[0].location");
    }

    @Test
    void emptyRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstShift(input).put("requiredSkill", "");

        assertRejected(post(input), "requiredSkill");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("employees", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.employees");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstShift(input).remove("start");

        assertRejected(post(input), "start");
    }

    @Test
    void offsetlessDateTimeIsRejected() {
        ObjectNode input = demoData();
        firstShift(input).put("end", "2026-10-26T14:00:00");

        assertRejected(post(input), "end");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstEmployee(input).put("skills", "Nurse");

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

    private static ObjectNode firstEmployee(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("employees").get(0);
    }

    private static ObjectNode firstShift(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("shifts").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
