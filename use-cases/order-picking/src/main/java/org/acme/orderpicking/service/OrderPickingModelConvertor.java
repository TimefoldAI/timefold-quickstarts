package org.acme.orderpicking.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.acme.orderpicking.dto.OrderPickingConfigOverrides;
import org.acme.orderpicking.dto.OrderPickingInput;
import org.acme.orderpicking.dto.OrderPickingOutput;
import org.acme.orderpicking.dto.PickTaskDTO;
import org.acme.orderpicking.dto.TrolleyDTO;
import org.acme.orderpicking.dto.WarehouseLocationDTO;
import org.acme.orderpicking.solver.OrderPickingConstraintProvider;

@ApplicationScoped
public class OrderPickingModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, OrderPickingInput, OrderPickingConfigOverrides, OrderPickingSolution, OrderPickingOutput> {

    @Override
    public OrderPickingInput applyOutputToInput(OrderPickingInput modelInput, OrderPickingOutput modelOutput) {
        Map<String, TrolleyDTO> outputTrolleys = modelOutput.trolleys().stream()
                .collect(Collectors.toMap(TrolleyDTO::id, trolley -> trolley));
        List<TrolleyDTO> updatedTrolleys = modelInput.trolleys().stream()
                .map(trolley -> {
                    TrolleyDTO solved = outputTrolleys.get(trolley.id());
                    if (solved == null) {
                        return trolley;
                    }
                    return trolley.withPickTaskIds(solved.pickTaskIds());
                })
                .collect(Collectors.toList());
        return new OrderPickingInput(updatedTrolleys, modelInput.pickTasks());
    }

    @Override
    public OrderPickingSolution toSolverModel(OrderPickingInput modelInput,
            ModelConfig<OrderPickingConfigOverrides> modelConfig, Optional<OrderPickingOutput> lastModelOutput) {
        Map<String, Order> orderMap = new HashMap<>();
        List<PickTask> pickTasks = modelInput.pickTasks().stream().map(dto -> {
            Order order = orderMap.computeIfAbsent(dto.orderId(), id -> new Order(id, new ArrayList<>()));
            Product product = new Product(dto.productId(), dto.productName(), dto.productVolume(), toLocation(dto.location()));
            OrderItem orderItem = new OrderItem(dto.id(), order, product);
            return new PickTask(dto.id(), orderItem);
        }).collect(Collectors.toList());
        Map<String, PickTask> pickTaskMap = pickTasks.stream()
                .collect(Collectors.toMap(PickTask::getId, pickTask -> pickTask));

        Map<String, Trolley> trolleyMap = new HashMap<>();
        List<Trolley> trolleys = modelInput.trolleys().stream().map(dto -> {
            Trolley trolley = new Trolley(dto.id(), dto.bucketCount(), dto.bucketCapacity(), toLocation(dto.location()));
            trolleyMap.put(trolley.getId(), trolley);
            return trolley;
        }).collect(Collectors.toList());

        assignPickTasks(modelInput.trolleys(), trolleyMap, pickTaskMap);

        OrderPickingSolution solution = new OrderPickingSolution(trolleys, pickTasks);
        applyConstraintWeightOverrides(solution, modelConfig);
        applyLastOutput(trolleyMap, pickTaskMap, lastModelOutput);
        return solution;
    }

    private static void assignPickTasks(List<TrolleyDTO> trolleyDTOs, Map<String, Trolley> trolleyMap,
            Map<String, PickTask> pickTaskMap) {
        for (TrolleyDTO dto : trolleyDTOs) {
            Trolley trolley = trolleyMap.get(dto.id());
            if (trolley == null) {
                continue;
            }
            trolley.getPickTasks().clear();
            for (String pickTaskId : dto.pickTaskIds()) {
                PickTask pickTask = pickTaskMap.get(pickTaskId);
                if (pickTask != null) {
                    trolley.getPickTasks().add(pickTask);
                }
            }
        }
    }

    private static void applyLastOutput(Map<String, Trolley> trolleyMap, Map<String, PickTask> pickTaskMap,
            Optional<OrderPickingOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        assignPickTasks(lastModelOutput.get().trolleys(), trolleyMap, pickTaskMap);
    }

    private static void applyConstraintWeightOverrides(OrderPickingSolution solution,
            ModelConfig<OrderPickingConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        OrderPickingConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weightOverrides = new HashMap<>();
        putIfPresent(weightOverrides, OrderPickingConstraintProvider.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK,
                overrides.minimizeDistanceFromPreviousPickWeight());
        putIfPresent(weightOverrides, OrderPickingConstraintProvider.MINIMIZE_DISTANCE_TO_PATH_ORIGIN,
                overrides.minimizeDistanceToPathOriginWeight());
        putIfPresent(weightOverrides, OrderPickingConstraintProvider.MINIMIZE_ORDER_SPLIT_BY_TROLLEY,
                overrides.minimizeOrderSplitByTrolleyWeight());
        if (!weightOverrides.isEmpty()) {
            solution.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weightOverrides));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    @Override
    public OrderPickingOutput toModelOutput(OrderPickingSolution solverModel) {
        List<TrolleyDTO> trolleys = solverModel.getTrolleys().stream().map(this::toDTO).collect(Collectors.toList());
        List<PickTaskDTO> pickTasks = solverModel.getPickTasks().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new OrderPickingOutput(trolleys, pickTasks, score);
    }

    private TrolleyDTO toDTO(Trolley trolley) {
        List<String> pickTaskIds = trolley.getPickTasks().stream().map(PickTask::getId).collect(Collectors.toList());
        return new TrolleyDTO(trolley.getId(), trolley.getBucketCount(), trolley.getBucketCapacity(),
                toDTO(trolley.getLocation()), pickTaskIds);
    }

    private PickTaskDTO toDTO(PickTask pickTask) {
        Product product = pickTask.getOrderItem().getProduct();
        return new PickTaskDTO(pickTask.getId(), pickTask.getOrderItem().getOrderId(), product.getId(),
                product.getName(), product.getVolume(), toDTO(product.getLocation()));
    }

    private WarehouseLocationDTO toDTO(WarehouseLocation location) {
        return new WarehouseLocationDTO(location.getShelvingId(), location.getSide().name(), location.getRow());
    }

    private static WarehouseLocation toLocation(WarehouseLocationDTO dto) {
        return new WarehouseLocation(dto.shelvingId(), Shelving.Side.valueOf(dto.side()), dto.row());
    }
}
