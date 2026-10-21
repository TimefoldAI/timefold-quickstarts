package org.acme.employeescheduling.solver;

import static org.acme.employeescheduling.support.TestHelper.createProblem;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInputMetrics;
import org.acme.employeescheduling.dto.output.EmployeeScheduleOutputMetrics;
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
        EmployeeSchedule problem =
                modelConvertor.toSolverModel(createProblem(), ModelConfig.empty(), Optional.empty());

        EmployeeScheduleOutputMetrics unsolvedMetrics = problem.getOutputMetrics();
        assertThat(unsolvedMetrics.totalAssignedShifts()).isZero();
        assertThat(unsolvedMetrics.totalUnassignedShifts()).isEqualTo(problem.getShifts().size());

        EmployeeSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getShifts()).allMatch(Shift::isAssigned);

        EmployeeScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.employees()).isEqualTo(3);
        assertThat(inputMetrics.shifts()).isEqualTo(4);
        assertThat(inputMetrics.locations()).isEqualTo(1);
        assertThat(inputMetrics.days()).isEqualTo(2);

        EmployeeScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedShifts()).isEqualTo(solution.getShifts().size());
        assertThat(outputMetrics.totalUnassignedShifts()).isZero();
    }
}
