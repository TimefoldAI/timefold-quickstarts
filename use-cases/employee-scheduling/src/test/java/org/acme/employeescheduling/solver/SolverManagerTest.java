package org.acme.employeescheduling.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.dto.EmployeeScheduleInputMetrics;
import org.acme.employeescheduling.dto.EmployeeScheduleOutputMetrics;
import org.acme.employeescheduling.service.EmployeeScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<EmployeeSchedule> solverManager;

    @Inject
    EmployeeScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        EmployeeSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        EmployeeSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        EmployeeScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.shifts()).isEqualTo(solution.getShifts().size());
        assertThat(inputMetrics.employees()).isEqualTo(solution.getEmployees().size());

        EmployeeScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedShifts()).isEqualTo(solution.getShifts().size());
        assertThat(outputMetrics.totalUnassignedShifts()).isZero();
        assertThat(outputMetrics.totalUsedEmployees()).isPositive();
    }
}
