package org.acme.maintenancescheduling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInputMetrics;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleOutputMetrics;
import org.acme.maintenancescheduling.service.MaintenanceScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<MaintenanceSchedule> solverManager;

    @Inject
    MaintenanceScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        MaintenanceSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        MaintenanceSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        MaintenanceScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.jobs()).isEqualTo(solution.getJobs().size());
        assertThat(inputMetrics.crews()).isEqualTo(solution.getCrews().size());
        assertThat(inputMetrics.tags()).isPositive();

        MaintenanceScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedJobs()).isEqualTo(solution.getJobs().size());
        assertThat(outputMetrics.totalUnassignedJobs()).isZero();
        assertThat(outputMetrics.totalUsedCrews()).isPositive();
    }
}
