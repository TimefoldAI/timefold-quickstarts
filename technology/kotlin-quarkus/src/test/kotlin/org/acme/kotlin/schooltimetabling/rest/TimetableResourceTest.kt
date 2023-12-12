package org.acme.kotlin.schooltimetabling.rest

import ai.timefold.solver.core.api.solver.SolverStatus
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.acme.kotlin.schooltimetabling.domain.Room
import org.acme.kotlin.schooltimetabling.domain.Timeslot
import org.acme.kotlin.schooltimetabling.domain.Timetable
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

@QuarkusTest
class TimetableResourceTest {

    @Test
    fun solveDemoDataUntilFeasible() {
        val testTimetable: Timetable = given()
            .`when`()["/demo-data/SMALL"]
            .then()
            .statusCode(200)
            .extract()
            .`as`(Timetable::class.java)

        val jobId: String = given()
            .contentType(ContentType.JSON)
            .body(testTimetable)
            .expect().contentType(ContentType.TEXT)
            .`when`().post("/timetables")
            .then()
            .statusCode(200)
            .extract()
            .asString()

        await()
            .atMost(Duration.ofMinutes(1))
            .pollInterval(Duration.ofMillis(500L))
            .until {
                SolverStatus.NOT_SOLVING.name ==
                        get("/timetables/$jobId/status")
                            .jsonPath().get("solverStatus")
            }
        val solution: Timetable =
            get("/timetables/$jobId").then().extract().`as`<Timetable>(
                Timetable::class.java
            )
        assertEquals(solution.solverStatus, SolverStatus.NOT_SOLVING)
        assertNotNull(solution.lessons)
        assertNotNull(solution.timeslots)
        assertNotNull(solution.rooms)
        assertNotNull(solution.lessons.get(0).room)
        assertNotNull(solution.lessons.get(0).timeslot)
        assertTrue(solution.score?.isFeasible!!)
    }

    @Test
    fun analyze() {
        val testTimetable: Timetable = given()
            .`when`()["/demo-data/SMALL"]
            .then()
            .statusCode(200)
            .extract()
            .`as`(Timetable::class.java)

        val roomList: List<Room> = testTimetable.rooms
        val timeslotList: List<Timeslot> = testTimetable.timeslots
        var i = 0
        for (lesson in testTimetable.lessons) { // Initialize the solution.
            lesson.room = roomList[i % roomList.size]
            lesson.timeslot = timeslotList[i % timeslotList.size]
            i += 1
        }
        val analysis: String = given()
            .contentType(ContentType.JSON)
            .body(testTimetable)
            .expect().contentType(ContentType.JSON)
            .`when`()
            .put("/timetables/analyze")
            .then()
            .extract()
            .asString()
        assertNotNull(analysis) // Too long to validate in its entirety.

        val analysis2: String = given()
            .contentType(ContentType.JSON)
            .queryParam("fetchPolicy", "FETCH_SHALLOW")
            .body(testTimetable)
            .expect().contentType(ContentType.JSON)
            .`when`()
            .put("/timetables/analyze")
            .then()
            .extract()
            .asString()
        assertNotNull(analysis2) // Too long to validate in its entirety.
    }
}
