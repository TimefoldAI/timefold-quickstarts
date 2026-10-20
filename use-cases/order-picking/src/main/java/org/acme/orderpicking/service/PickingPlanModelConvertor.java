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
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.PickingPlanConstraintProperties;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;
import org.acme.orderpicking.dto.output.PickingPlanOutput;
import org.acme.orderpicking.dto.output.TrolleyOutputDTO;

@ApplicationScoped
public class PickingPlanModelConvertor
        implements ModelConvertor<HardSoftScore, PickingPlanInput, PickingPlanConfigOverrides, PickingPlan, PickingPlanOutput> {

    @Override
    public PickingPlanInput applyOutputToInput(PickingPlanInput modelInput, PickingPlanOutput modelOutput) {
        // The assignment is the pick task list of each trolley, so overlaying the output means replacing one list
        // per trolley.
        Map<String, TrolleyOutputDTO> outputTrolleys = new LinkedHashMap<>();
        for (TrolleyOutputDTO trolley : modelOutput.trolleys()) {
            outputTrolleys.put(trolley.id(), trolley);
        }
        List<TrolleyInputDTO> updatedTrolleys = modelInput.trolleys().stream()
                .map(trolley -> {
                    TrolleyOutputDTO solved = outputTrolleys.get(trolley.id());
                    return solved == null || solved.pickTaskIds() == null ? trolley
                            : trolley.withPickTaskIds(solved.pickTaskIds());
                })
                .toList();
        return modelInput.withTrolleys(updatedTrolleys);
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
        List<Trolley> trolleys = modelInput.trolleys().stream()
                .map(dto -> new Trolley(dto.id(), dto.bucketCount(), dto.bucketCapacity(), toLocation(dto.location())))
                .toList();

        PickingPlan pickingPlan = new PickingPlan(trolleys, List.copyOf(pickTaskMap.values()));
        applyPaths(trolleys, pickTaskMap, modelInput, lastModelOutput);
        applyConstraintWeightOverrides(pickingPlan, modelConfig);
        return pickingPlan;
    }

    @Override
    public PickingPlanOutput toModelOutput(PickingPlan solverModel) {
        List<TrolleyOutputDTO> trolleys = solverModel.getTrolleys().stream()
                .map(trolley -> new TrolleyOutputDTO(trolley.getId(),
                        trolley.getPickTasks().stream().map(PickTask::getId).toList(),
                        Warehouse.calculateDistanceToTravel(trolley)))
                .toList();
        return new PickingPlanOutput(trolleys);
    }

    private static WarehouseLocation toLocation(WarehouseLocationInputDTO dto) {
        return new WarehouseLocation(dto.shelvingId(), dto.side(), dto.row());
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
        if (overrides.minimizeOrderSplitByTrolleyWeight() != null) {
            weights.put(PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_TROLLEY,
                    HardSoftScore.ofSoft(overrides.minimizeOrderSplitByTrolleyWeight()));
        }
        if (!weights.isEmpty()) {
            pickingPlan.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    /**
     * Fills the list variable of every trolley: from lastModelOutput when a halted run is being recovered, and from
     * the input's own paths otherwise. The shadow variables (trolley, previousPickTask, nextPickTask) are deliberately
     * not set here - the solver derives them when it loads the solution.
     */
    private static void applyPaths(List<Trolley> trolleys, Map<String, PickTask> pickTaskMap,
            PickingPlanInput modelInput, Optional<PickingPlanOutput> lastModelOutput) {
        Map<String, List<String>> pathByTrolleyId = lastModelOutput
                .map(output -> output.trolleys().stream()
                        .filter(trolley -> trolley.pickTaskIds() != null)
                        .collect(Collectors.toMap(TrolleyOutputDTO::id, TrolleyOutputDTO::pickTaskIds)))
                .orElseGet(() -> modelInput.trolleys().stream()
                        .collect(Collectors.toMap(TrolleyInputDTO::id, TrolleyInputDTO::pickTaskIds)));

        for (Trolley trolley : trolleys) {
            List<String> pickTaskIds = pathByTrolleyId.get(trolley.getId());
            if (pickTaskIds == null || pickTaskIds.isEmpty()) {
                continue;
            }
            // The solver mutates this list, so it cannot be an immutable copy of the input's.
            List<PickTask> path = new ArrayList<>(pickTaskIds.size());
            for (String pickTaskId : pickTaskIds) {
                path.add(require(pickTaskMap, pickTaskId, "pick task"));
            }
            trolley.setPickTasks(path);
        }
    }
}
