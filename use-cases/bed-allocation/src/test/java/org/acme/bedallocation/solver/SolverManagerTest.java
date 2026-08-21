package org.acme.bedallocation.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.dto.BedPlanInputMetrics;
import org.acme.bedallocation.dto.BedPlanOutputMetrics;
import org.acme.bedallocation.service.BedPlanModelConvertor;
import org.acme.bedallocation.support.BedPlanTestDataFactory;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<BedPlan> solverManager;

    @Inject
    BedPlanModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = BedPlanTestDataFactory.createProblem();

        BedPlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        BedPlan solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        BedPlanInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.stays()).isEqualTo(solution.getStays().size());
        assertThat(inputMetrics.departments()).isEqualTo(solution.getDepartments().size());
        assertThat(inputMetrics.rooms()).isEqualTo(solution.getRooms().size());
        assertThat(inputMetrics.beds()).isEqualTo(solution.getBeds().size());

        BedPlanOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedStays()).isEqualTo(solution.getStays().size());
        assertThat(outputMetrics.totalUnassignedStays()).isZero();
        assertThat(outputMetrics.totalUsedRooms()).isPositive();
        assertThat(outputMetrics.totalUsedBeds()).isPositive();
    }
}
