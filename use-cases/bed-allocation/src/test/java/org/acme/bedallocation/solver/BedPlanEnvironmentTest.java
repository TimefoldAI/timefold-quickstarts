package org.acme.bedallocation.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.service.BedPlanModelConvertor;
import org.acme.bedallocation.support.BedPlanTestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class BedPlanEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    BedPlanModelConvertor modelConvertor;

    @Test
    void solveFullAssert() {
        solve(EnvironmentMode.FULL_ASSERT);
    }

    @Test
    void solveStepAssert() {
        solve(EnvironmentMode.STEP_ASSERT);
    }

    void solve(EnvironmentMode environmentMode) {
        var input = BedPlanTestDataFactory.createProblem();
        BedPlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<BedPlan> solverFactory = SolverFactory.create(updatedConfig);

        Solver<BedPlan> solver = solverFactory.buildSolver();
        BedPlan solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
