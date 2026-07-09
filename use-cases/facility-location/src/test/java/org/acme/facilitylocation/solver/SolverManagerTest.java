package org.acme.facilitylocation.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.core.api.solver.SolverManager;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.dto.FacilityLocationInputMetrics;
import org.acme.facilitylocation.dto.FacilityLocationOutputMetrics;
import org.acme.facilitylocation.service.FacilityLocationModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<FacilityLocationProblem> solverManager;

    @Inject
    FacilityLocationModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = SolverTestDataFactory.createProblem();

        FacilityLocationProblem problem = modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty());

        FacilityLocationProblem solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();

        FacilityLocationInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.facilities()).isEqualTo(solution.getFacilities().size());
        assertThat(inputMetrics.consumers()).isEqualTo(solution.getConsumers().size());
        assertThat(inputMetrics.totalDemand()).isPositive();
        assertThat(inputMetrics.totalCapacity()).isPositive();
        assertThat(inputMetrics.totalPotentialSetupCost()).isPositive();

        FacilityLocationOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalActivatedFacilities()).isPositive();
        assertThat(outputMetrics.totalActivatedFacilities() + outputMetrics.totalUnusedFacilities())
                .isEqualTo(solution.getFacilities().size());
        assertThat(outputMetrics.totalAssignedConsumers()).isEqualTo(solution.getConsumers().size());
        assertThat(outputMetrics.totalUnassignedConsumers()).isZero();
        assertThat(outputMetrics.totalSetupCost()).isPositive();
        assertThat(outputMetrics.totalTravelDistanceMeters()).isPositive();
        assertThat(outputMetrics.averageTravelDistanceMetersPerConsumer()).isNotNull().isPositive();
        assertThat(outputMetrics.capacityUtilizationPercentage()).isNotNull();
        assertThat(outputMetrics.capacityUtilizationPercentage()).isBetween(0.0, 100.0);
    }

}
