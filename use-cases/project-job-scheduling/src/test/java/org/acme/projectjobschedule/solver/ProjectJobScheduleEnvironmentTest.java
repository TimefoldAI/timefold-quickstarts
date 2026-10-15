package org.acme.projectjobschedule.solver;

import static org.acme.projectjobschedule.support.TestHelper.createProblem;
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

import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.service.ProjectJobScheduleModelConvertor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class ProjectJobScheduleEnvironmentTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    ProjectJobScheduleModelConvertor modelConvertor;

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
        ProjectJobSchedule problem = modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(EnvironmentMode.FULL_ASSERT).withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        updatedConfig.withMonitoringConfig(new MonitoringConfig().withSolverMetricList(List.of()));
        if (moveThreadCount != null) {
            updatedConfig.withMoveThreadCount(moveThreadCount);
        }
        SolverFactory<ProjectJobSchedule> solverFactory = SolverFactory.create(updatedConfig);

        Solver<ProjectJobSchedule> solver = solverFactory.buildSolver();
        ProjectJobSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
