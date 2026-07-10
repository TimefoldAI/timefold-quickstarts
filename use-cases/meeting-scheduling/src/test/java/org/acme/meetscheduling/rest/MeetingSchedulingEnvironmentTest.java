package org.acme.meetscheduling.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.meetingschedule.domain.MeetingSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class MeetingSchedulingEnvironmentTest {

    @Inject
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
        // Load the problem
        MeetingSchedule problem = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(MeetingSchedule.class);

        // Update the environment
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<MeetingSchedule> solverFactory = SolverFactory.create(updatedConfig);

        // Solve the problem
        Solver<MeetingSchedule> solver = solverFactory.buildSolver();
        MeetingSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}