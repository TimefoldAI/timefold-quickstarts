package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class VehicleRoutingEnvironmentTest {

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
        VehicleRoutePlan problem = given()
                .when().get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        // Update the environment
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<VehicleRoutePlan> solverFactory = SolverFactory.create(updatedConfig);

        // Solve the problem
        Solver<VehicleRoutePlan> solver = solverFactory.buildSolver();
        VehicleRoutePlan solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}