package org.acme.taskassigning.solver;

import static org.acme.taskassigning.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.acme.taskassigning.dto.input.TaskAssigningInputMetrics;
import org.acme.taskassigning.dto.output.TaskAssigningOutputMetrics;
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
        var input = createProblem();

        TaskAssigningSolution problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        TaskAssigningSolution solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        TaskAssigningInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.tasks()).isEqualTo(solution.getTasks().size());
        assertThat(inputMetrics.employees()).isEqualTo(solution.getEmployees().size());
        assertThat(inputMetrics.customers()).isEqualTo(solution.getCustomers().size());
        assertThat(inputMetrics.taskTypes()).isEqualTo(solution.getTaskTypes().size());

        TaskAssigningOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedTasks()).isEqualTo(solution.getTasks().size());
        assertThat(outputMetrics.totalUnassignedTasks()).isZero();
        assertThat(outputMetrics.totalUsedEmployees()).isPositive();
    }
}
