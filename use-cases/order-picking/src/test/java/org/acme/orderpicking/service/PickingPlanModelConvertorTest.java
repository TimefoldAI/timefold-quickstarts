package org.acme.orderpicking.service;

import static org.acme.orderpicking.support.TestHelper.configOverrides;
import static org.acme.orderpicking.support.TestHelper.createProblem;
import static org.acme.orderpicking.support.TestHelper.input;
import static org.acme.orderpicking.support.TestHelper.output;
import static org.acme.orderpicking.support.TestHelper.pickTaskDTO;
import static org.acme.orderpicking.support.TestHelper.pickingCartDTO;
import static org.acme.orderpicking.support.TestHelper.pickingCartOutputDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Optional;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingCart;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.PickingPlanConstraintProperties;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.dto.input.PickingCartInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.output.PickingCartOutputDTO;
import org.acme.orderpicking.dto.output.PickingPlanOutput;
import org.junit.jupiter.api.Test;

class PickingPlanModelConvertorTest {

    private static final PickingPlanInput PROBLEM = createProblem();

    private final PickingPlanModelConvertor convertor = new PickingPlanModelConvertor();

    @Test
    void unassignedInputLeavesEveryPickTaskToTheSolver() {
        PickingPlan pickingPlan = toSolverModel(PROBLEM, Optional.empty());

        assertThat(pickingPlan.getPickingCarts()).hasSize(2).allMatch(pickingCart -> pickingCart.getPickTasks().isEmpty());
        assertThat(pickingPlan.getPickTasks()).hasSize(4);
        assertThat(pickingPlan.getPickTasks().getFirst().getProduct().id()).isEqualTo("p1");
    }

    @Test
    void assignedInputIsKept() {
        PickingPlanInput input = PROBLEM.withPickingCarts(List.of(pickingCartDTO("c1", "2-1", "1-0"), pickingCartDTO("c2")));

        PickingCart pickingCart = pickingCartById(toSolverModel(input, Optional.empty()), "c1");
        assertThat(pickingCart.getPickTasks()).extracting(PickTask::getId).containsExactly("2-1", "1-0");
    }

    @Test
    void lastModelOutputOverridesTheInputAssignment() {
        PickingPlanInput input = PROBLEM.withPickingCarts(List.of(pickingCartDTO("c1", "2-1", "1-0"), pickingCartDTO("c2")));
        PickingPlanOutput lastOutput = output(List.of(
                pickingCartOutputDTO("c1", List.of()),
                pickingCartOutputDTO("c2", List.of("1-1", "2-0"))));

        PickingPlan pickingPlan = toSolverModel(input, Optional.of(lastOutput));
        assertThat(pickingCartById(pickingPlan, "c1").getPickTasks()).isEmpty();
        assertThat(pickingCartById(pickingPlan, "c2").getPickTasks()).extracting(PickTask::getId)
                .containsExactly("1-1", "2-0");
    }

    @Test
    void unknownProductReferenceFailsFast() {
        PickingPlanInput input =
                input(PROBLEM.products(), List.of(pickTaskDTO("1-0", "1", "does-not-exist")), PROBLEM.pickingCarts());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toSolverModel(input, Optional.empty()))
                .withMessageContaining("product 'does-not-exist'");
    }

    @Test
    void unknownPickTaskReferenceFailsFast() {
        PickingPlanInput input = PROBLEM.withPickingCarts(List.of(pickingCartDTO("c1", "does-not-exist")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toSolverModel(input, Optional.empty()))
                .withMessageContaining("pick task 'does-not-exist'");
    }

    @Test
    void outputRoundTripsThroughTheInput() {
        PickingPlan pickingPlan = toSolverModel(PROBLEM, Optional.empty());
        PickingCart firstPickingCart = pickingPlan.getPickingCarts().getFirst();
        firstPickingCart.getPickTasks().addAll(pickingPlan.getPickTasks());

        PickingPlanOutput output = convertor.toModelOutput(pickingPlan);
        assertThat(output.pickingCarts()).extracting(PickingCartOutputDTO::id).containsExactly("c1", "c2");
        PickingCartOutputDTO firstOutputPickingCart = output.pickingCarts().getFirst();
        assertThat(firstOutputPickingCart.pickTaskIds()).containsExactly("1-0", "1-1", "2-0", "2-1");
        assertThat(firstOutputPickingCart.travelDistance()).isEqualTo(Warehouse.calculateDistanceToTravel(firstPickingCart));
        assertThat(output.pickingCarts().getLast().pickTaskIds()).isEmpty();
        assertThat(output.pickingCarts().getLast().travelDistance()).isZero();

        PickingPlanInput updatedInput = convertor.applyOutputToInput(PROBLEM, output);
        assertThat(updatedInput.pickingCarts()).extracting(PickingCartInputDTO::pickTaskIds)
                .containsExactly(List.of("1-0", "1-1", "2-0", "2-1"), List.of());
        assertThat(updatedInput.products()).isEqualTo(PROBLEM.products());
        assertThat(updatedInput.pickTasks()).isEqualTo(PROBLEM.pickTasks());
    }

    @Test
    void onlySetWeightsAreOverridden() {
        ModelConfig<PickingPlanConfigOverrides> modelConfig = new ModelConfig<>(configOverrides(5L, null, 0L));

        PickingPlan pickingPlan = convertor.toSolverModel(PROBLEM, modelConfig, Optional.empty());
        var overrides = pickingPlan.getConstraintWeightOverrides();
        assertThat(overrides.getKnownConstraintIds()).containsExactlyInAnyOrder(
                PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK,
                PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_PICKING_CART);
        assertThat(overrides.getConstraintWeight(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK))
                .isEqualTo(HardSoftScore.ofSoft(5));
        assertThat(overrides.getConstraintWeight(PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_PICKING_CART))
                .isEqualTo(HardSoftScore.ZERO);
    }

    private PickingPlan toSolverModel(PickingPlanInput input, Optional<PickingPlanOutput> lastOutput) {
        return convertor.toSolverModel(input, ModelConfig.empty(), lastOutput);
    }

    private static PickingCart pickingCartById(PickingPlan pickingPlan, String id) {
        return pickingPlan.getPickingCarts().stream().filter(pickingCart -> pickingCart.getId().equals(id)).findFirst()
                .orElseThrow();
    }
}
