package org.acme.projectjobschedule.rest;

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
class ProjectJobScheduleOpenApiValidationTest {

    @Inject
    ObjectMapper mapper;

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstProject(input).putNull("id");

        assertRejected(post(input), "projects[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("jobs", mapper.createArrayNode());

        assertRejected(post(input), "modelInput.jobs");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstResource(input).put("capacity", 0);

        assertRejected(post(input), "capacity");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstAllocation(input).remove("jobId");

        assertRejected(post(input), "jobId");
    }

    @Test
    void negativeDelayIsRejected() {
        ObjectNode input = demoData();
        firstAllocation(input).put("delay", -1);

        assertRejected(post(input), "delay");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstAllocation(input).put("delay", "not-a-number");

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

    private ObjectNode firstProject(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("projects").get(0);
    }

    private ObjectNode firstResource(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("resources").get(0);
    }

    private ObjectNode firstAllocation(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("allocations").get(0);
    }

    private Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/schedules");
    }

    private void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
