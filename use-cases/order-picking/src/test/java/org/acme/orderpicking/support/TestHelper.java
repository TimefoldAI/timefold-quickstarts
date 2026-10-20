package org.acme.orderpicking.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.solver.SolutionManager;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingCart;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.RackSide;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.acme.orderpicking.dto.input.PickTaskInputDTO;
import org.acme.orderpicking.dto.input.PickingCartInputDTO;
import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;
import org.acme.orderpicking.dto.input.ProductInputDTO;
import org.acme.orderpicking.dto.input.WarehouseLocationInputDTO;
import org.acme.orderpicking.dto.output.PickingCartOutputDTO;
import org.acme.orderpicking.dto.output.PickingPlanOutput;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private TestHelper() {
    }

    // ************************************************************************
    // DTOs
    // ************************************************************************

    public static PickingPlanInput input(List<ProductInputDTO> products, List<PickTaskInputDTO> pickTasks,
            List<PickingCartInputDTO> pickingCarts) {
        return new PickingPlanInput(products, pickTasks, pickingCarts);
    }

    /**
     * A small but feasible problem: three products, two orders of two items each, and two picking carts that start in
     * the top left corner of the warehouse.
     */
    public static PickingPlanInput createProblem() {
        List<ProductInputDTO> products = List.of(
                productDTO("p1", 500, locationDTO("A1", RackSide.RIGHT, 3)),
                productDTO("p2", 800, locationDTO("B2", RackSide.LEFT, 5)),
                productDTO("p3", 300, locationDTO("C3", RackSide.RIGHT, 8)));
        List<PickTaskInputDTO> pickTasks = List.of(
                pickTaskDTO("1-0", "1", "p1"),
                pickTaskDTO("1-1", "1", "p2"),
                pickTaskDTO("2-0", "2", "p2"),
                pickTaskDTO("2-1", "2", "p3"));
        List<PickingCartInputDTO> pickingCarts = List.of(pickingCartDTO("c1"), pickingCartDTO("c2"));
        return input(products, pickTasks, pickingCarts);
    }

    public static PickingPlanConfigOverrides configOverrides(Long minimizeDistanceFromPreviousPickTaskWeight,
            Long minimizeDistanceFromLastPickTaskToPathOriginWeight, Long minimizeOrderSplitByPickingCartWeight) {
        return new PickingPlanConfigOverrides(minimizeDistanceFromPreviousPickTaskWeight,
                minimizeDistanceFromLastPickTaskToPathOriginWeight, minimizeOrderSplitByPickingCartWeight);
    }

    public static WarehouseLocationInputDTO locationDTO(String rackId, RackSide side, int bay) {
        return new WarehouseLocationInputDTO(rackId, side, bay);
    }

    public static ProductInputDTO productDTO(String id, int volume, WarehouseLocationInputDTO location) {
        return new ProductInputDTO(id, "Product " + id, volume, location);
    }

    /** A product of 100 cm3 on rack A1. */
    public static ProductInputDTO productDTO(String id) {
        return productDTO(id, 100, locationDTO("A1", RackSide.LEFT, 1));
    }

    public static PickTaskInputDTO pickTaskDTO(String id, String orderId, String productId) {
        return new PickTaskInputDTO(id, orderId, productId);
    }

    /** A picking cart of 4 buckets of 1000 cm3 each, that starts at A1 LEFT 0 with no path yet. */
    public static PickingCartInputDTO pickingCartDTO(String id, String... pickTaskIds) {
        return pickingCartDTO(id, 1000, locationDTO("A1", RackSide.LEFT, 0), pickTaskIds);
    }

    public static PickingCartInputDTO pickingCartDTO(String id, int bucketCapacity, WarehouseLocationInputDTO location,
            String... pickTaskIds) {
        return new PickingCartInputDTO(id, 4, bucketCapacity, location, List.of(pickTaskIds));
    }

    public static PickingCartOutputDTO pickingCartOutputDTO(String id, List<String> pickTaskIds) {
        return new PickingCartOutputDTO(id, pickTaskIds, 0);
    }

    public static PickingPlanOutput output(List<PickingCartOutputDTO> pickingCarts) {
        return new PickingPlanOutput(pickingCarts);
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    public static WarehouseLocation location(String rackId, RackSide side, int bay) {
        return new WarehouseLocation(rackId, side, bay);
    }

    public static Product product(String id, int volume, WarehouseLocation location) {
        return new Product(id, "Product " + id, volume, location);
    }

    /** A pick task of a product of the given volume, on rack A1. */
    public static PickTask pickTask(String id, String orderId, int volume) {
        return pickTask(id, orderId, product("p-" + id, volume, location("A1", RackSide.LEFT, 1)));
    }

    /** A pick task of a product of 1 cm3 at the given location. */
    public static PickTask pickTask(String id, WarehouseLocation location) {
        return pickTask(id, "order", product("p-" + id, 1, location));
    }

    public static PickTask pickTask(String id, String orderId, Product product) {
        return new PickTask(id, orderId, product);
    }

    /**
     * Creates a picking cart that starts at A1 LEFT 0 and picks the given pick tasks in that order.
     */
    public static PickingCart pickingCart(String id, int bucketCount, int bucketCapacity, PickTask... pickTasks) {
        return pickingCart(id, bucketCount, bucketCapacity, location("A1", RackSide.LEFT, 0), pickTasks);
    }

    /**
     * Creates a picking cart that picks the given pick tasks in that order, and sets the shadow variables of those pick
     * tasks (picking cart, previous and next pick task) the way the solver would: ConstraintVerifier does not compute them.
     */
    public static PickingCart pickingCart(String id, int bucketCount, int bucketCapacity, WarehouseLocation location,
            PickTask... pickTasks) {
        PickingCart pickingCart = new PickingCart(id, bucketCount, bucketCapacity, location);
        pickingCart.setPickTasks(new ArrayList<>(List.of(pickTasks)));
        Object[] entities = Stream.concat(Stream.of(pickingCart), Stream.of(pickTasks)).toArray();
        SolutionManager.updateShadowVariables(PickingPlan.class, entities);
        return pickingCart;
    }
}
