package org.acme.foodpackaging.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.dto.PackagingScheduleInputMetrics;
import org.acme.foodpackaging.dto.PackagingScheduleOutputMetrics;
import org.acme.foodpackaging.service.PackagingScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<PackagingSchedule> solverManager;

    @Inject
    PackagingScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        PackagingSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        PackagingSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        PackagingScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.jobs()).isEqualTo(solution.getJobs().size());
        assertThat(inputMetrics.lines()).isEqualTo(solution.getLines().size());

        PackagingScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedJobs()).isEqualTo(solution.getJobs().size());
        assertThat(outputMetrics.totalUnassignedJobs()).isZero();
        assertThat(outputMetrics.totalUsedLines()).isPositive();
    }
}
