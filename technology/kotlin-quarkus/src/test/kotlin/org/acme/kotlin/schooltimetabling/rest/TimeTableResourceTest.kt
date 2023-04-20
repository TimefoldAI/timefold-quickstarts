package org.acme.kotlin.schooltimetabling.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.awaitility.Awaitility.await
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import ai.timefold.solver.core.api.solver.SolverStatus
import java.time.Duration

@QuarkusTest
class TimeTableResourceTest {

    @Test
    fun solveDemoDataUntilFeasible() {
        given()
            .contentType(ContentType.JSON)
            .`when`().post("/timeTable/solve")
            .then()
            .statusCode(204)

        await()
            .atMost(Duration.ofMinutes(1))
            .pollDelay(Duration.ofSeconds(5))
            .pollInterval(Duration.ofSeconds(5))
            .until {
                SolverStatus.NOT_SOLVING.name == get("/timeTable").body().path("solverStatus")
            }

        get("/timeTable").then().assertThat()
            .body("solverStatus", Matchers.equalTo(SolverStatus.NOT_SOLVING.name))
            .body("timeslotList", Matchers.`is`(Matchers.not(Matchers.empty<Any>())))
            .body("roomList", Matchers.`is`(Matchers.not(Matchers.empty<Any>())))
            .body("lessonList", Matchers.`is`(Matchers.not(Matchers.empty<Any>())))
            .body("lessonList.timeslot", Matchers.not(Matchers.nullValue()))
            .body("lessonList.room", Matchers.not(Matchers.nullValue()))
    }

}
