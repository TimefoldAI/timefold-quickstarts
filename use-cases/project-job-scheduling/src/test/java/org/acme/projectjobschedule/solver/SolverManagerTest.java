package org.acme.projectjobschedule.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.projectjobschedule.demo.DemoDataBuilder;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInputMetrics;
import org.acme.projectjobschedule.dto.ProjectJobScheduleOutputMetrics;
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
        ProjectJobScheduleInput input = createProblem();

        ProjectJobSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        ProjectJobSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        ProjectJobScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.projects()).isEqualTo(solution.getProjects().size());
        assertThat(inputMetrics.jobs()).isEqualTo(solution.getJobs().size());
        assertThat(inputMetrics.resources()).isEqualTo(solution.getResources().size());
        assertThat(inputMetrics.executionModes()).isEqualTo(solution.getExecutionModes().size());
        assertThat(inputMetrics.allocations()).isEqualTo(solution.getAllocations().size());

        ProjectJobScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalMakespan()).isPositive();
        assertThat(outputMetrics.totalScheduledAllocations()).isEqualTo(solution.getAllocations().size());
        assertThat(outputMetrics.totalUnscheduledAllocations()).isZero();
    }

    static ProjectJobScheduleInput createProblem() {
        return DemoDataBuilder.builder()
                .setJobCount(24)
                .addProject("0", 0, 10)
                .addProject("1", 4, 19)
                .addGlobalResource("0", 16)
                .addLocalResource("1", "0", 13, true)
                .addLocalResource("2", "0", 44, false)
                .addLocalResource("3", "0", 39, false)
                .addLocalResource("4", "1", 24, true)
                .addLocalResource("5", "1", 66, false)
                .addLocalResource("6", "1", 56, false)
                .build();
    }
}
