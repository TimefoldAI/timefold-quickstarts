package ai.timefold.solver.quickstarts.all.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import ai.timefold.solver.quickstarts.all.domain.QuickstartMeta;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class QuickstartLauncherResourceTest {

    @Test
    public void getQuickstartMetaList() {
        List<QuickstartMeta> quickstartMetaList = given()
                .when().get("/quickstart")
                .then()
                .statusCode(200)
                .extract().body().jsonPath()
                .getList(".", QuickstartMeta.class);
        assertFalse(quickstartMetaList.isEmpty());
    }

}
