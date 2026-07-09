package org.acme.taskassigning.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.acme.taskassigning.dto.TaskAssigningInputMetrics;
import org.acme.taskassigning.dto.TaskAssigningOutputMetrics;
import org.acme.taskassigning.service.TaskAssigningModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<TaskAssigningSolution> solverManager;

    @Inject
    TaskAssigningModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        TaskAssigningSolution problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        TaskAssigningSolution solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().isFeasible()).isTrue();

        TaskAssigningInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.employees()).isEqualTo(solution.getEmployees().size());
        assertThat(inputMetrics.tasks()).isEqualTo(solution.getTasks().size());
        assertThat(inputMetrics.taskTypes()).isPositive();
        assertThat(inputMetrics.customers()).isPositive();

        TaskAssigningOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.assignedTasks()).isEqualTo(solution.getTasks().size());
        assertThat(outputMetrics.unassignedTasks()).isZero();
        assertThat(outputMetrics.usedEmployees()).isPositive();
        assertThat(outputMetrics.makespan()).isNotNegative();
    }
}
