package org.acme.facilitylocation.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class FacilityPlanOpenApiValidationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void validInputIsAccepted() {
        post(demoData()).then().statusCode(202);
    }

    @Test
    void nullRequiredStringIsRejected() {
        ObjectNode input = demoData();
        firstFacility(input).putNull("id");

        assertRejected(post(input), "facilities[0].id");
    }

    @Test
    void emptyRequiredCollectionIsRejected() {
        ObjectNode input = demoData();
        modelInput(input).set("consumers", MAPPER.createArrayNode());

        assertRejected(post(input), "modelInput.consumers");
    }

    @Test
    void belowMinimumNumberIsRejected() {
        ObjectNode input = demoData();
        firstFacility(input).put("capacity", 0);

        assertRejected(post(input), "capacity");
    }

    @Test
    void missingRequiredFieldIsRejected() {
        ObjectNode input = demoData();
        firstFacility(input).remove("capacity");

        assertRejected(post(input), "capacity");
    }

    @Test
    void missingRequiredNestedDtoIsRejected() {
        ObjectNode input = demoData();
        firstConsumer(input).remove("location");

        assertRejected(post(input), "location");
    }

    @Test
    void outOfRangeLatitudeIsRejected() {
        ObjectNode input = demoData();
        ((ObjectNode) firstFacility(input).get("location")).put("latitude", 91);

        assertRejected(post(input), "latitude");
    }

    @Test
    void mismatchedJsonTypeIsRejected() {
        ObjectNode input = demoData();
        firstFacility(input).put("capacity", "not-a-number");

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

    private static ObjectNode firstFacility(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("facilities").get(0);
    }

    private static ObjectNode firstConsumer(ObjectNode input) {
        return (ObjectNode) modelInput(input).get("consumers").get(0);
    }

    private static Response post(ObjectNode input) {
        return given().contentType(ContentType.JSON).body(input.toString()).when().post("/v1/plans");
    }

    private static void assertRejected(Response response, String expectedFieldFragment) {
        response.then().statusCode(400);
        assertThat(response.getBody().asString()).contains(expectedFieldFragment);
    }
}
