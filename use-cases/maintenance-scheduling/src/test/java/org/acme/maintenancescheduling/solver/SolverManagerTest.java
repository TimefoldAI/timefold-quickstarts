package org.acme.maintenancescheduling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInputMetrics;
import org.acme.maintenancescheduling.dto.output.MaintenanceScheduleOutputMetrics;
import org.acme.maintenancescheduling.service.MaintenanceScheduleModelConvertor;
import org.acme.maintenancescheduling.support.TestHelper;
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
        var input = TestHelper.createProblem();

        MaintenanceSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        MaintenanceSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getJobs()).allMatch(Job::isAssigned);

        MaintenanceScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.jobs()).isEqualTo(solution.getJobs().size());
        assertThat(inputMetrics.crews()).isEqualTo(solution.getCrews().size());
        assertThat(inputMetrics.workdays()).isEqualTo(solution.createStartDateList().size());

        MaintenanceScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedJobs()).isEqualTo(solution.getJobs().size());
        assertThat(outputMetrics.totalUnassignedJobs()).isZero();
        assertThat(outputMetrics.totalUsedCrews()).isPositive();
        assertThat(outputMetrics.totalOverdueJobs()).isNotNegative();
    }
}
