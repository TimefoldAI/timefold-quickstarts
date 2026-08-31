package org.acme.foodpackaging.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.monitoring.MonitoringConfig;

import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.persistence.PackagingScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class FoodPackingEnvironmentTest {

    @Inject
    SolverConfig solverConfig;
    @Inject
    PackagingScheduleRepository repository;

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
        PackagingSchedule problem = repository.read();

        // Update the environment
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(environmentMode)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        updatedConfig.withMonitoringConfig(new MonitoringConfig().withSolverMetricList(List.of()));
        SolverFactory<PackagingSchedule> solverFactory = SolverFactory.create(updatedConfig);

        // Solve the problem
        Solver<PackagingSchedule> solver = solverFactory.buildSolver();
        PackagingSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}