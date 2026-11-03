package org.acme.vehiclerouting.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInputMetrics;
import org.acme.vehiclerouting.dto.output.VehicleRoutePlanOutputMetrics;
import org.acme.vehiclerouting.service.VehicleRoutePlanModelConvertor;
import org.acme.vehiclerouting.support.TestHelper;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<VehicleRoutePlan> solverManager;

    @Inject
    VehicleRoutePlanModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = TestHelper.createProblem();

        VehicleRoutePlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());
        TestHelper.initDistanceMap(problem);

        VehicleRoutePlan solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getVisits()).allMatch(Visit::isAssigned);

        VehicleRoutePlanInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.visits()).isEqualTo(solution.getVisits().size());
        assertThat(inputMetrics.vehicles()).isEqualTo(solution.getVehicles().size());
        assertThat(inputMetrics.totalDemand()).isPositive();
        assertThat(inputMetrics.totalCapacity()).isGreaterThanOrEqualTo(inputMetrics.totalDemand());

        VehicleRoutePlanOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedVisits()).isEqualTo(solution.getVisits().size());
        assertThat(outputMetrics.totalUnassignedVisits()).isZero();
        assertThat(outputMetrics.totalUsedVehicles()).isPositive();
        assertThat(outputMetrics.totalDrivingTimeSeconds()).isPositive();
    }
}
