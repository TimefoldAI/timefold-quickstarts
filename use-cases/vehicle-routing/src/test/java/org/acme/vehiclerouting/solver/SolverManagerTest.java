package org.acme.vehiclerouting.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.dto.VehicleRoutingInputMetrics;
import org.acme.vehiclerouting.dto.VehicleRoutingOutputMetrics;
import org.acme.vehiclerouting.service.VehicleRoutingModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<VehicleRoutePlan> solverManager;

    @Inject
    VehicleRoutingModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        VehicleRoutePlan problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        VehicleRoutePlan solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().isFeasible()).isTrue();

        VehicleRoutingInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.vehicles()).isEqualTo(solution.getVehicles().size());
        assertThat(inputMetrics.visits()).isEqualTo(solution.getVisits().size());

        VehicleRoutingOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.assignedVisits()).isEqualTo(solution.getVisits().size());
        assertThat(outputMetrics.unassignedVisits()).isZero();
        assertThat(outputMetrics.usedVehicles()).isPositive();
        assertThat(outputMetrics.totalDrivingTimeSeconds()).isNotNegative();
    }
}
