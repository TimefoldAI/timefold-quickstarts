package org.acme.facilitylocation.solver;

import static org.acme.facilitylocation.support.TestHelper.createProblem;
import static org.acme.facilitylocation.support.TestHelper.initDistanceMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityPlan;
import org.acme.facilitylocation.dto.input.FacilityPlanInputMetrics;
import org.acme.facilitylocation.dto.output.FacilityPlanOutputMetrics;
import org.acme.facilitylocation.service.FacilityPlanModelConvertor;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SolverManagerTest {

    @Inject
    SolverManager<FacilityPlan> solverManager;

    @Inject
    FacilityPlanModelConvertor modelConvertor;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        var input = createProblem();

        FacilityPlan problem = initDistanceMap(modelConvertor.toSolverModel(input, ModelConfig.empty(), Optional.empty()));

        FacilityPlan solution = solverManager.solveBuilder().withProblemId(0L)
                .withProblemFinder(id -> problem).run().getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
        assertThat(solution.getConsumers()).allMatch(consumer -> consumer.getFacility() != null);

        FacilityPlanInputMetrics inputMetrics = solution.getInputMetrics();
        assertThat(inputMetrics.facilities()).isEqualTo(solution.getFacilities().size());
        assertThat(inputMetrics.consumers()).isEqualTo(solution.getConsumers().size());
        assertThat(inputMetrics.totalCapacity())
                .isEqualTo(solution.getFacilities().stream().mapToLong(Facility::getCapacity).sum());
        assertThat(inputMetrics.totalDemand())
                .isEqualTo(solution.getConsumers().stream().mapToLong(Consumer::getDemand).sum());

        FacilityPlanOutputMetrics outputMetrics = solution.getOutputMetrics();
        assertThat(outputMetrics.totalAssignedConsumers()).isEqualTo(solution.getConsumers().size());
        assertThat(outputMetrics.totalUnassignedConsumers()).isZero();
        assertThat(outputMetrics.totalUsedFacilities()).isPositive();
        assertThat(outputMetrics.totalUsedFacilities())
                .isEqualTo((int) solution.getConsumers().stream().map(Consumer::getFacility).filter(Objects::nonNull)
                        .distinct().count());
        assertThat(outputMetrics.totalSetupCost()).isPositive();
        assertThat(outputMetrics.totalDistanceInMeters()).isNotNegative();
    }
}
