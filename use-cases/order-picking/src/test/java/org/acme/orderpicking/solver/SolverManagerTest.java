package org.acme.orderpicking.solver;

import static org.acme.orderpicking.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.dto.input.PickingPlanInputMetrics;
import org.acme.orderpicking.dto.output.PickingPlanOutputMetrics;
import org.acme.orderpicking.service.PickingPlanModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<PickingPlan> solverManager;

    @Inject
    PickingPlanModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        PickingPlan problem = modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        PickingPlanOutputMetrics unsolvedMetrics = problem.getOutputMetrics();
        assertThat(unsolvedMetrics.totalTravelDistance()).isZero();
        assertThat(unsolvedMetrics.usedPickingCarts()).isZero();
        assertThat(unsolvedMetrics.splitOrders()).isZero();

        PickingPlan solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getPickTasks()).allMatch(pickTask -> pickTask.getPickingCart() != null);
        assertThat(solution.getPickingCarts().stream().mapToInt(pickingCart -> pickingCart.getPickTasks().size()).sum())
                .isEqualTo(solution.getPickTasks().size());

        PickingPlanInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.pickTasks()).isEqualTo(4);
        assertThat(inputMetrics.orders()).isEqualTo(2);
        assertThat(inputMetrics.products()).isEqualTo(3);
        assertThat(inputMetrics.pickingCarts()).isEqualTo(2);

        PickingPlanOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalTravelDistance()).isPositive();
        assertThat(outputMetrics.usedPickingCarts()).isBetween(1, 2);
        assertThat(outputMetrics.splitOrders()).isBetween(0, 2);
        assertThat(solution.getPickTasks()).extracting(PickTask::getId).doesNotHaveDuplicates();
    }
}
