package org.acme.projectjobschedule.solver;

import static org.acme.projectjobschedule.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInputMetrics;
import org.acme.projectjobschedule.dto.output.ProjectJobScheduleOutputMetrics;
import org.acme.projectjobschedule.service.ProjectJobScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<ProjectJobSchedule> solverManager;

    @Inject
    ProjectJobScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        ProjectJobSchedule problem = modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        ProjectJobSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        ProjectJobScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.allocations()).isEqualTo(solution.getAllocations().size());
        assertThat(inputMetrics.jobs()).isEqualTo(solution.getJobs().size());
        assertThat(inputMetrics.projects()).isEqualTo(solution.getProjects().size());
        assertThat(inputMetrics.resources()).isEqualTo(solution.getResources().size());

        ProjectJobScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedAllocations()).isEqualTo(solution.getAllocations().size());
        assertThat(outputMetrics.totalUnassignedAllocations()).isZero();
        assertThat(outputMetrics.totalMakespan()).isPositive();
        assertThat(outputMetrics.totalProjectDelay()).isNotNegative();
    }
}
