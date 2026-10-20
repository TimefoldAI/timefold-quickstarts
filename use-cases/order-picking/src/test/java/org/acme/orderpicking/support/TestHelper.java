package org.acme.orderpicking.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.solver.SolutionManager;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.ShelvingSide;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.TrolleyInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;
import org.acme.orderpicking.dto.output.PickingPlanOutput;
import org.acme.orderpicking.dto.output.TrolleyOutputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private TestHelper() {
    }

    // ************************************************************************
    // DTOs
    // ************************************************************************

    public static PickingPlanInput input(List<ProductInputDTO> products, List<PickTaskInputDTO> pickTasks,
            List<TrolleyInputDTO> trolleys) {
        return new PickingPlanInput(products, pickTasks, trolleys);
    }

    /**
     * A small but feasible problem: three products, two orders of two items each, and two trolleys that start in
     * the top left corner of the warehouse.
     */
    public static PickingPlanInput createProblem() {
        List<ProductInputDTO> products = List.of(
                productDTO("p1", 500, locationDTO("A1", ShelvingSide.RIGHT, 3)),
                productDTO("p2", 800, locationDTO("B2", ShelvingSide.LEFT, 5)),
                productDTO("p3", 300, locationDTO("C3", ShelvingSide.RIGHT, 8)));
        List<PickTaskInputDTO> pickTasks = List.of(
                pickTaskDTO("1-0", "1", "p1"),
                pickTaskDTO("1-1", "1", "p2"),
                pickTaskDTO("2-0", "2", "p2"),
                pickTaskDTO("2-1", "2", "p3"));
        List<TrolleyInputDTO> trolleys = List.of(trolleyDTO("t1"), trolleyDTO("t2"));
        return input(products, pickTasks, trolleys);
    }

    public static PickingPlanConfigOverrides configOverrides(Long minimizeDistanceFromPreviousPickTaskWeight,
            Long minimizeDistanceFromLastPickTaskToPathOriginWeight, Long minimizeOrderSplitByTrolleyWeight) {
        return new PickingPlanConfigOverrides(minimizeDistanceFromPreviousPickTaskWeight,
                minimizeDistanceFromLastPickTaskToPathOriginWeight, minimizeOrderSplitByTrolleyWeight);
    }

    public static WarehouseLocationInputDTO locationDTO(String shelvingId, ShelvingSide side, int row) {
        return new WarehouseLocationInputDTO(shelvingId, side, row);
    }

    public static ProductInputDTO productDTO(String id, int volume, WarehouseLocationInputDTO location) {
        return new ProductInputDTO(id, "Product " + id, volume, location);
    }

    /** A product of 100 cm3 on shelving A1. */
    public static ProductInputDTO productDTO(String id) {
        return productDTO(id, 100, locationDTO("A1", ShelvingSide.LEFT, 1));
    }

    public static PickTaskInputDTO pickTaskDTO(String id, String orderId, String productId) {
        return new PickTaskInputDTO(id, orderId, productId);
    }

    /** A trolley of 4 buckets of 1000 cm3 each, that starts at A1 LEFT 0 with no path yet. */
    public static TrolleyInputDTO trolleyDTO(String id, String... pickTaskIds) {
        return trolleyDTO(id, 1000, locationDTO("A1", ShelvingSide.LEFT, 0), pickTaskIds);
    }

    public static TrolleyInputDTO trolleyDTO(String id, int bucketCapacity, WarehouseLocationInputDTO location,
            String... pickTaskIds) {
        return new TrolleyInputDTO(id, 4, bucketCapacity, location, List.of(pickTaskIds));
    }

    public static TrolleyOutputDTO trolleyOutputDTO(String id, List<String> pickTaskIds) {
        return new TrolleyOutputDTO(id, pickTaskIds, 0);
    }

    public static PickingPlanOutput output(List<TrolleyOutputDTO> trolleys) {
        return new PickingPlanOutput(trolleys);
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    public static WarehouseLocation location(String shelvingId, ShelvingSide side, int row) {
        return new WarehouseLocation(shelvingId, side, row);
    }

    public static Product product(String id, int volume, WarehouseLocation location) {
        return new Product(id, "Product " + id, volume, location);
    }

    /** A pick task of a product of the given volume, on shelving A1. */
    public static PickTask pickTask(String id, String orderId, int volume) {
        return pickTask(id, orderId, product("p-" + id, volume, location("A1", ShelvingSide.LEFT, 1)));
    }

    /** A pick task of a product of 1 cm3 at the given location. */
    public static PickTask pickTask(String id, WarehouseLocation location) {
        return pickTask(id, "order", product("p-" + id, 1, location));
    }

    public static PickTask pickTask(String id, String orderId, Product product) {
        return new PickTask(id, orderId, product);
    }

    /**
     * Creates a trolley that starts at A1 LEFT 0 and picks the given pick tasks in that order.
     */
    public static Trolley trolley(String id, int bucketCount, int bucketCapacity, PickTask... pickTasks) {
        return trolley(id, bucketCount, bucketCapacity, location("A1", ShelvingSide.LEFT, 0), pickTasks);
    }

    /**
     * Creates a trolley that picks the given pick tasks in that order, and sets the shadow variables of those pick
     * tasks (trolley, previous and next pick task) the way the solver would: ConstraintVerifier does not compute them.
     */
    public static Trolley trolley(String id, int bucketCount, int bucketCapacity, WarehouseLocation location,
            PickTask... pickTasks) {
        Trolley trolley = new Trolley(id, bucketCount, bucketCapacity, location);
        trolley.setPickTasks(new ArrayList<>(List.of(pickTasks)));
        Object[] entities = Stream.concat(Stream.of(trolley), Stream.of(pickTasks)).toArray();
        SolutionManager.updateShadowVariables(PickingPlan.class, entities);
        return trolley;
    }
}
