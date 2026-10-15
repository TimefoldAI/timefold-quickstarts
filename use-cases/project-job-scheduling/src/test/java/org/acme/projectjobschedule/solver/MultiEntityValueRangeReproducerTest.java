package org.acme.projectjobschedule.solver;

import static org.acme.projectjobschedule.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.monitoring.MonitoringConfig;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.service.ProjectJobScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * REPRODUCER: {@link org.acme.projectjobschedule.domain.Allocation} has entity-level value ranges,
 * {@link org.acme.projectjobschedule.domain.ProjectLead} has a solution-level one.
 * Solving with the default local search phase crashes as soon as the swap move selector runs.
 */
@QuarkusTest
class MultiEntityValueRangeReproducerTest {

    @Inject
    SolverConfig solverConfig;

    @Inject
    ProjectJobScheduleModelConvertor modelConvertor;

    @Test
    void solveWithLocalSearch() {
        ProjectJobSchedule problem = modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        SolverConfig updatedConfig = solverConfig.copyConfig();
        // No best score limit, so the local search phase actually runs.
        updatedConfig.withTerminationSpentLimit(Duration.ofSeconds(5))
                .getTerminationConfig().withBestScoreLimit(null);
        updatedConfig.withMonitoringConfig(new MonitoringConfig().withSolverMetricList(List.of()));

        ProjectJobSchedule solution = SolverFactory.<ProjectJobSchedule> create(updatedConfig).buildSolver().solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
