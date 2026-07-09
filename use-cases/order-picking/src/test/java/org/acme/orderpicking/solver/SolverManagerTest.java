package org.acme.orderpicking.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.dto.OrderPickingInputMetrics;
import org.acme.orderpicking.dto.OrderPickingOutputMetrics;
import org.acme.orderpicking.service.OrderPickingModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<OrderPickingSolution> solverManager;

    @Inject
    OrderPickingModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        OrderPickingSolution problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        OrderPickingSolution solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        OrderPickingInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.trolleys()).isEqualTo(solution.getTrolleys().size());
        assertThat(inputMetrics.pickTasks()).isEqualTo(solution.getPickTasks().size());
        assertThat(inputMetrics.orders()).isPositive();
        assertThat(inputMetrics.products()).isPositive();
        assertThat(inputMetrics.totalVolume()).isPositive();

        OrderPickingOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedPickTasks()).isEqualTo(solution.getPickTasks().size());
        assertThat(outputMetrics.totalUnassignedPickTasks()).isZero();
        assertThat(outputMetrics.totalUsedTrolleys()).isPositive();
        assertThat(outputMetrics.totalDistanceToTravel()).isNotNegative();
    }
}
