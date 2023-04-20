package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ai.timefold.solver.core.api.solver.SolverStatus;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Duration;

@SpringBootTest(properties = {
        // Effectively disable spent-time termination in favor of the best-score-limit
        "optaplanner.solver.termination.spent-limit=1h",
        "optaplanner.solver.termination.best-score-limit=0hard/*soft" },
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TimeTableControllerTest {

    @Test
    @Timeout(600_000)
    public void solveDemoDataUntilFeasible() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/timeTable/solve")
                .then()
                .statusCode(200);

        await()
                .atMost(Duration.ofMinutes(1))
                .pollDelay(Duration.ofSeconds(5))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(get("/timeTable").body().path("solverStatus")));

        get("/timeTable").then().assertThat()
                .body("solverStatus", equalTo(SolverStatus.NOT_SOLVING.name()))
                .body("timeslotList", is(not(empty())))
                .body("roomList", is(not(empty())))
                .body("lessonList", is(not(empty())))
                .body("lessonList.timeslot", not(nullValue()))
                .body("lessonList.room", not(nullValue()));
    }

}
