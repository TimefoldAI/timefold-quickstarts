package org.acme.orderpicking.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class PickingPlanOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        Response response = post(demoData());
        assertThat(response.getStatusCode()).as(response.getBody().asString()).isEqualTo(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstPickTask(input).putNull("orderId");

        assertRejected(post(input), "pickTasks[0].orderId");
    }

    @Test
    void emptyRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstPickTask(input).put("productId", "");

        assertRejected(post(input), "productId");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("trolleys", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.trolleys");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstTrolley(input).remove("location");

        assertRejected(post(input), "location");
    }

    @Test
    void unknownShelvingSideIsRejected() {
        ObjectNode input = demoData();
        ((ObjectNode) firstProduct(input).get("location")).put("side", "MIDDLE");

        assertRejected(post(input), "side");
    }

    @Test
    void rowOutOfRangeIsRejected() {
        ObjectNode input = demoData();
        ((ObjectNode) firstProduct(input).get("location")).put("row", 11);

        assertRejected(post(input), "row");
    }

    @Test
    void zeroBucketCountIsRejected() {
        ObjectNode input = demoData();
        firstTrolley(input).put("bucketCount", 0);

        assertRejected(post(input), "bucketCount");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstProduct(input).put("volume", "large");

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

    private static ObjectNode firstProduct(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("products").get(0);
    }

    private static ObjectNode firstPickTask(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("pickTasks").get(0);
    }

    private static ObjectNode firstTrolley(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("trolleys").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/picking-plans");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
