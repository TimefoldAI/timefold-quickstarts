package org.acme.schooltimetabling.rest;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.JsonNode;


@SpringBootTest(properties = {
        // Effectively disable spent-time termination in favor of the best-score-limit
        "timefold.solver.termination.spent-limit=1h",
        "timefold.solver.termination.best-score-limit=0hard/*soft" },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimetableControllerTest {

    @LocalServerPort
    String port;

    @Test
    void solve() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Timetable testTimetable = client.get()
                .uri("/demo-data/SMALL")
                .exchange()
                .expectBody(Timetable.class)
                .returnResult()
                .getResponseBody();

        String jobId = client.post()
                .uri("/timetables")
                .bodyValue(testTimetable)
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        client.get()
                                .uri("/timetables/" + jobId + "/status")
                                .exchange()
                                .expectBody(JsonNode.class)
                                .returnResult()
                                .getResponseBody()
                                .get("solverStatus")
                                .asString()));

        client.get()
                .uri("/timetables/" + jobId)
                .exchange()
                .expectBody()
                .jsonPath("solverStatus").isEqualTo("NOT_SOLVING")
                .jsonPath("timeslots").isArray()
                .jsonPath("timeslots").isNotEmpty()
                .jsonPath("rooms").isArray()
                .jsonPath("rooms").isNotEmpty()
                .jsonPath("lessons[0].room").isNotEmpty()
                .jsonPath("lessons[0].timeslot").isNotEmpty()
                .jsonPath("score").isNotEmpty();
    }

    @EnabledIfSystemProperty(named = "enterprise", matches = ".*")
    @Test
    void analyze() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Timetable testTimetable = client.get()
                .uri("/demo-data/SMALL")
                .exchange()
                .expectBody(Timetable.class)
                .returnResult()
                .getResponseBody();

        var roomList = testTimetable.getRooms();
        var timeslotList = testTimetable.getTimeslots();
        int i = 0;
        for (var lesson : testTimetable.getLessons()) { // Initialize the solution.
            lesson.setRoom(roomList.get(i % roomList.size()));
            lesson.setTimeslot(timeslotList.get(i % timeslotList.size()));
            i += 1;
        }

        client.put()
                .uri("/timetables/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testTimetable)
                .exchange()
                .expectBody()
                .jsonPath("score").isNotEmpty();

        client.put()
                .uri("/timetables/analyze?fetchPolicy=FETCH_SHALLOW")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testTimetable)
                .exchange()
                .expectBody()
                .jsonPath("score").isNotEmpty();
    }

}
