package org.acme.orderpicking.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingCart;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.PickingPlanConstraintProperties;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingCartInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;
import org.acme.orderpicking.dto.output.PickingCartOutputDTO;
import org.acme.orderpicking.dto.output.PickingPlanOutput;

@ApplicationScoped
public class PickingPlanModelConvertor
        implements ModelConvertor<HardSoftScore, PickingPlanInput, PickingPlanConfigOverrides, PickingPlan, PickingPlanOutput> {

    @Override
    public PickingPlanInput applyOutputToInput(PickingPlanInput modelInput, PickingPlanOutput modelOutput) {
        // The assignment is the pick task list of each picking cart, so overlaying the output means replacing one list
        // per picking cart.
        Map<String, PickingCartOutputDTO> outputPickingCarts = new LinkedHashMap<>();
        for (PickingCartOutputDTO pickingCart : modelOutput.pickingCarts()) {
            outputPickingCarts.put(pickingCart.id(), pickingCart);
        }
        List<PickingCartInputDTO> updatedPickingCarts = modelInput.pickingCarts().stream()
                .map(pickingCart -> {
                    PickingCartOutputDTO solved = outputPickingCarts.get(pickingCart.id());
                    return solved == null || solved.pickTaskIds() == null ? pickingCart
                            : pickingCart.withPickTaskIds(solved.pickTaskIds());
                })
                .toList();
        return modelInput.withPickingCarts(updatedPickingCarts);
    }

    @Override
    public PickingPlan toSolverModel(PickingPlanInput modelInput, ModelConfig<PickingPlanConfigOverrides> modelConfig,
            Optional<PickingPlanOutput> lastModelOutput) {
        Map<String, Product> productMap = new LinkedHashMap<>();
        for (ProductInputDTO dto : modelInput.products()) {
            productMap.put(dto.id(), new Product(dto.id(), dto.name(), dto.volume(), toLocation(dto.location())));
        }
        Map<String, PickTask> pickTaskMap = new LinkedHashMap<>();
        for (PickTaskInputDTO dto : modelInput.pickTasks()) {
            pickTaskMap.put(dto.id(),
                    new PickTask(dto.id(), dto.orderId(), require(productMap, dto.productId(), "product")));
        }
        List<PickingCart> pickingCarts = modelInput.pickingCarts().stream()
                .map(dto -> new PickingCart(dto.id(), dto.bucketCount(), dto.bucketCapacity(), toLocation(dto.location())))
                .toList();

        PickingPlan pickingPlan = new PickingPlan(pickingCarts, List.copyOf(pickTaskMap.values()));
        applyPaths(pickingCarts, pickTaskMap, modelInput, lastModelOutput);
        applyConstraintWeightOverrides(pickingPlan, modelConfig);
        return pickingPlan;
    }

    @Override
    public PickingPlanOutput toModelOutput(PickingPlan solverModel) {
        List<PickingCartOutputDTO> pickingCarts = solverModel.getPickingCarts().stream()
                .map(pickingCart -> new PickingCartOutputDTO(pickingCart.getId(),
                        pickingCart.getPickTasks().stream().map(PickTask::getId).toList(),
                        Warehouse.calculateDistanceToTravel(pickingCart)))
                .toList();
        return new PickingPlanOutput(pickingCarts);
    }

    private static WarehouseLocation toLocation(WarehouseLocationInputDTO dto) {
        return new WarehouseLocation(dto.rackId(), dto.side(), dto.bay());
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(PickingPlan pickingPlan,
            ModelConfig<PickingPlanConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        PickingPlanConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftScore> weights = new HashMap<>();
        if (overrides.minimizeDistanceFromPreviousPickTaskWeight() != null) {
            weights.put(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK,
                    HardSoftScore.ofSoft(overrides.minimizeDistanceFromPreviousPickTaskWeight()));
        }
        if (overrides.minimizeDistanceFromLastPickTaskToPathOriginWeight() != null) {
            weights.put(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_LAST_PICK_TASK_TO_PATH_ORIGIN,
                    HardSoftScore.ofSoft(overrides.minimizeDistanceFromLastPickTaskToPathOriginWeight()));
        }
        if (overrides.minimizeOrderSplitByPickingCartWeight() != null) {
            weights.put(PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_PICKING_CART,
                    HardSoftScore.ofSoft(overrides.minimizeOrderSplitByPickingCartWeight()));
        }
        if (!weights.isEmpty()) {
            pickingPlan.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    /**
     * Fills the list variable of every picking cart: from lastModelOutput when a halted run is being recovered, and from
     * the input's own paths otherwise. The shadow variables (pickingCart, previousPickTask, nextPickTask) are deliberately
     * not set here - the solver derives them when it loads the solution.
     */
    private static void applyPaths(List<PickingCart> pickingCarts, Map<String, PickTask> pickTaskMap,
            PickingPlanInput modelInput, Optional<PickingPlanOutput> lastModelOutput) {
        Map<String, List<String>> pathByPickingCartId = lastModelOutput
                .map(output -> output.pickingCarts().stream()
                        .filter(pickingCart -> pickingCart.pickTaskIds() != null)
                        .collect(Collectors.toMap(PickingCartOutputDTO::id, PickingCartOutputDTO::pickTaskIds)))
                .orElseGet(() -> modelInput.pickingCarts().stream()
                        .collect(Collectors.toMap(PickingCartInputDTO::id, PickingCartInputDTO::pickTaskIds)));

        for (PickingCart pickingCart : pickingCarts) {
            List<String> pickTaskIds = pathByPickingCartId.get(pickingCart.getId());
            if (pickTaskIds == null || pickTaskIds.isEmpty()) {
                continue;
            }
            // The solver mutates this list, so it cannot be an immutable copy of the input's.
            List<PickTask> path = new ArrayList<>(pickTaskIds.size());
            for (String pickTaskId : pickTaskIds) {
                path.add(require(pickTaskMap, pickTaskId, "pick task"));
            }
            pickingCart.setPickTasks(path);
        }
    }
}
