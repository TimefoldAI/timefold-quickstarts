package org.acme.facilitylocation.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.service.FacilityLocationModelConvertor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class FacilityLocationEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    FacilityLocationModelConvertor modelConvertor;

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
        var input = SolverTestDataFactory.createProblem();
        FacilityLocationProblem problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        // Update the environment
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<FacilityLocationProblem> solverFactory = SolverFactory.create(updatedConfig);

        // Solve the problem
        Solver<FacilityLocationProblem> solver = solverFactory.buildSolver();
        FacilityLocationProblem solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
