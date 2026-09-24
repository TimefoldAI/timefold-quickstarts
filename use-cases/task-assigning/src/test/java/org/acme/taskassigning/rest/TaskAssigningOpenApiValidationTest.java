package org.acme.taskassigning.rest;

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
class TaskAssigningOpenApiValidationTest {

    @Inject
    ObjectMapper mapper;

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstCustomer(input).putNull("id");

        assertRejected(post(input), "customers[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("tasks", mapper.createArrayNode());

        assertRejected(post(input), "modelInput.tasks");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstTaskType(input).put("baseDurationInMinutes", 0);

        assertRejected(post(input), "baseDurationInMinutes");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstTaskType(input).remove("baseDurationInMinutes");

        assertRejected(post(input), "baseDurationInMinutes");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstTaskType(input).put("baseDurationInMinutes", "not-a-number");

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

    private static ObjectNode modelInput(ObjectNode input) {
        return (ObjectNode) input.get("modelInput");
    }

    private static ObjectNode firstCustomer(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("customers").get(0);
    }

    private static ObjectNode firstTaskType(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("taskTypes").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
