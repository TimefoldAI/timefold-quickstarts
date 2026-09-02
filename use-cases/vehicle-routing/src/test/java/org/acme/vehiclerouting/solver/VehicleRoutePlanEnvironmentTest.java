package org.acme.vehiclerouting.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.monitoring.MonitoringConfig;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.service.VehicleRoutePlanModelConvertor;
import org.acme.vehiclerouting.support.TestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class VehicleRoutePlanEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    VehicleRoutePlanModelConvertor modelConvertor;

    @Test
    void solveFullAssert() {
        solve(null);
    }

    // Multithreaded solving is a Timefold Solver Enterprise Edition feature, so these only run
    // when the enterprise Maven profile (-Denterprise) is active.
    @Test
    @EnabledIfSystemProperty(named = "timefold.solver.enterprise", matches = "true")
    void solveFullAssertMultithreaded() {
        solve(SolverConfig.MOVE_THREAD_COUNT_AUTO);
    }

    void solve(String moveThreadCount) {
        var input = TestHelper.createProblem();
        VehicleRoutePlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(EnvironmentMode.FULL_ASSERT).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        updatedConfig.withMonitoringConfig(new MonitoringConfig().withSolverMetricList(List.of()));
        if (moveThreadCount != null) {
            updatedConfig.withMoveThreadCount(moveThreadCount);
        }
        SolverFactory<VehicleRoutePlan> solverFactory = SolverFactory.create(updatedConfig);

        Solver<VehicleRoutePlan> solver = solverFactory.buildSolver();
        VehicleRoutePlan solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
