package org.acme.bedallocation.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.BedSchedule;
import org.acme.bedallocation.dto.BedScheduleInputMetrics;
import org.acme.bedallocation.dto.BedScheduleOutputMetrics;
import org.acme.bedallocation.service.BedScheduleModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<BedSchedule> solverManager;

    @Inject
    BedScheduleModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        BedSchedule problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        BedSchedule solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        BedScheduleInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.stays()).isEqualTo(solution.getStays().size());
        assertThat(inputMetrics.beds()).isEqualTo(solution.getBeds().size());

        BedScheduleOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedStays()).isEqualTo(solution.getStays().size());
        assertThat(outputMetrics.totalUnassignedStays()).isZero();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
    }
}
