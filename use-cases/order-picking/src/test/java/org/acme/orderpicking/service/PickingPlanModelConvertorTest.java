package org.acme.orderpicking.service;

import static org.acme.orderpicking.support.TestHelper.configOverrides;
import static org.acme.orderpicking.support.TestHelper.createProblem;
import static org.acme.orderpicking.support.TestHelper.input;
import static org.acme.orderpicking.support.TestHelper.output;
import static org.acme.orderpicking.support.TestHelper.pickTaskDTO;
import static org.acme.orderpicking.support.TestHelper.trolleyDTO;
import static org.acme.orderpicking.support.TestHelper.trolleyOutputDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Optional;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.PickingPlanConstraintProperties;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.acme.orderpicking.dto.output.PickingPlanOutput;
import org.acme.orderpicking.dto.output.TrolleyOutputDTO;
import org.junit.jupiter.api.Test;

class PickingPlanModelConvertorTest {

    private static final PickingPlanInput PROBLEM = createProblem();

    private final PickingPlanModelConvertor convertor = new PickingPlanModelConvertor();

    @Test
    void unassignedInputLeavesEveryPickTaskToTheSolver() {
        PickingPlan pickingPlan = toSolverModel(PROBLEM, Optional.empty());

        assertThat(pickingPlan.getTrolleys()).hasSize(2).allMatch(trolley -> trolley.getPickTasks().isEmpty());
        assertThat(pickingPlan.getPickTasks()).hasSize(4);
        assertThat(pickingPlan.getPickTasks().getFirst().getProduct().id()).isEqualTo("p1");
    }

    @Test
    void assignedInputIsKept() {
        PickingPlanInput input = PROBLEM.withTrolleys(List.of(trolleyDTO("t1", "2-1", "1-0"), trolleyDTO("t2")));

        Trolley trolley = trolleyById(toSolverModel(input, Optional.empty()), "t1");
        assertThat(trolley.getPickTasks()).extracting(PickTask::getId).containsExactly("2-1", "1-0");
    }

    @Test
    void lastModelOutputOverridesTheInputAssignment() {
        PickingPlanInput input = PROBLEM.withTrolleys(List.of(trolleyDTO("t1", "2-1", "1-0"), trolleyDTO("t2")));
        PickingPlanOutput lastOutput = output(List.of(
                trolleyOutputDTO("t1", List.of()),
                trolleyOutputDTO("t2", List.of("1-1", "2-0"))));

        PickingPlan pickingPlan = toSolverModel(input, Optional.of(lastOutput));
        assertThat(trolleyById(pickingPlan, "t1").getPickTasks()).isEmpty();
        assertThat(trolleyById(pickingPlan, "t2").getPickTasks()).extracting(PickTask::getId)
                .containsExactly("1-1", "2-0");
    }

    @Test
    void unknownProductReferenceFailsFast() {
        PickingPlanInput input =
                input(PROBLEM.products(), List.of(pickTaskDTO("1-0", "1", "does-not-exist")), PROBLEM.trolleys());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toSolverModel(input, Optional.empty()))
                .withMessageContaining("product 'does-not-exist'");
    }

    @Test
    void unknownPickTaskReferenceFailsFast() {
        PickingPlanInput input = PROBLEM.withTrolleys(List.of(trolleyDTO("t1", "does-not-exist")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toSolverModel(input, Optional.empty()))
                .withMessageContaining("pick task 'does-not-exist'");
    }

    @Test
    void outputRoundTripsThroughTheInput() {
        PickingPlan pickingPlan = toSolverModel(PROBLEM, Optional.empty());
        Trolley firstTrolley = pickingPlan.getTrolleys().getFirst();
        firstTrolley.getPickTasks().addAll(pickingPlan.getPickTasks());

        PickingPlanOutput output = convertor.toModelOutput(pickingPlan);
        assertThat(output.trolleys()).extracting(TrolleyOutputDTO::id).containsExactly("t1", "t2");
        TrolleyOutputDTO firstOutputTrolley = output.trolleys().getFirst();
        assertThat(firstOutputTrolley.pickTaskIds()).containsExactly("1-0", "1-1", "2-0", "2-1");
        assertThat(firstOutputTrolley.travelDistance()).isEqualTo(Warehouse.calculateDistanceToTravel(firstTrolley));
        assertThat(output.trolleys().getLast().pickTaskIds()).isEmpty();
        assertThat(output.trolleys().getLast().travelDistance()).isZero();

        PickingPlanInput updatedInput = convertor.applyOutputToInput(PROBLEM, output);
        assertThat(updatedInput.trolleys()).extracting(TrolleyInputDTO::pickTaskIds)
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
                PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_TROLLEY);
        assertThat(overrides.getConstraintWeight(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK))
                .isEqualTo(HardSoftScore.ofSoft(5));
        assertThat(overrides.getConstraintWeight(PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_TROLLEY))
                .isEqualTo(HardSoftScore.ZERO);
    }

    private PickingPlan toSolverModel(PickingPlanInput input, Optional<PickingPlanOutput> lastOutput) {
        return convertor.toSolverModel(input, ModelConfig.empty(), lastOutput);
    }

    private static Trolley trolleyById(PickingPlan pickingPlan, String id) {
        return pickingPlan.getTrolleys().stream().filter(trolley -> trolley.getId().equals(id)).findFirst().orElseThrow();
    }
}
