package org.acme.schooltimetabling.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class TimetableEnvironmentTest {

    @LocalServerPort
    private int port;

    @Autowired
    SolverConfig solverConfig;

    @Test
    void solveFullAssert() {
        solve(EnvironmentMode.FULL_ASSERT);
    }

    @Test
    void solveStepAssert() {
        solve(EnvironmentMode.STEP_ASSERT);
    }

    void solve(EnvironmentMode environmentMode) {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // Load the problem
        Timetable problem = client.get()
                .uri("/demo-data/SMALL")
                .exchange()
                .expectBody(Timetable.class)
                .returnResult()
                .getResponseBody();

        // Update the environment
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<Timetable> solverFactory = SolverFactory.create(updatedConfig);

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}