package org.acme.orderpicking.solver;

import static org.acme.orderpicking.domain.Shelving.newShelvingId;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_A;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_C;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_D;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_E;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_2;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_3;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.solver.SolutionManager;
import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OrderPickingConstraintProviderTest {

    @Inject
    ConstraintVerifier<OrderPickingConstraintProvider, OrderPickingSolution> constraintVerifier;

    @Test
    void requiredNumberOfBucketsWithPenalization() {
        var order1 = createOrder("order1",
                createOrderItem(4), //goes in Trolley1
                createOrderItem(5), //goes in Trolley1
                createOrderItem(9), //goes in Trolley2
                createOrderItem(8)); //goes in Trolley2

        var order2 = createOrder("order2",
                createOrderItem(4), //goes in Trolley1
                createOrderItem(1), //goes in Trolley1
                createOrderItem(8), //goes in Trolley2
                createOrderItem(6), //goes in Trolley2
                createOrderItem(10), //goes in Trolley2
                createOrderItem(9)); //goes in Trolley2

        var trolley1PickTask1 = createPickTask(order1.getItems().get(0));
        var trolley1PickTask2 = createPickTask(order1.getItems().get(1));

        var trolley1PickTask3 = createPickTask(order2.getItems().get(0));
        var trolley1PickTask4 = createPickTask(order2.getItems().get(1));

        //Trolley1:
        //Order1 total volume = 9 -> requires 2 buckets
        //Order2 total volume = 5 -> requires 1 bucket
        //Total required buckets = 3
        //Penalization = 3 - 2 = 1
        var trolley1 = initializeTrolley(2, 5,
                trolley1PickTask1,
                trolley1PickTask2,
                trolley1PickTask3,
                trolley1PickTask4);

        var trolley2PickTask1 = createPickTask(order1.getItems().get(2));
        var trolley2PickTask2 = createPickTask(order1.getItems().get(3));

        var trolley2PickTask3 = createPickTask(order2.getItems().get(2));
        var trolley2PickTask4 = createPickTask(order2.getItems().get(3));
        var trolley2PickTask5 = createPickTask(order2.getItems().get(4));
        var trolley2PickTask6 = createPickTask(order2.getItems().get(5));

        //Trolley2:
        //Order1 total volume = 17 -> requires 2 bucket
        //Order2 total volume = 33 -> requires 4 buckets
        //Total required buckets = 6
        //Penalization = 6 - 2 = 4
        var trolley2 = initializeTrolley(2, 10,
                trolley2PickTask1,
                trolley2PickTask2,
                trolley2PickTask3,
                trolley2PickTask4,
                trolley2PickTask5,
                trolley2PickTask6);

        //Penalization Trolley1 = 1
        //Penalization Trolley2 = 4
        //Total penalization = 5
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::requiredNumberOfBuckets)
                .given(trolley1PickTask1,
                        trolley1PickTask2,
                        trolley1PickTask3,
                        trolley1PickTask4,
                        trolley2PickTask1,
                        trolley2PickTask2,
                        trolley2PickTask3,
                        trolley2PickTask4,
                        trolley2PickTask5,
                        trolley2PickTask6)
                .penalizesBy(5);
    }

    @Test
    void minimizeDistanceFromPreviousPickTask() {
        var currentPickTask =
                createPickTask(new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 1));
        var previousPickTask =
                createPickTask(new WarehouseLocation(newShelvingId(COL_E, ROW_1), Shelving.Side.RIGHT, 3));

        var trolley = initializeTrolley(1, 1,
                previousPickTask,
                currentPickTask);
        currentPickTask.setTrolley(trolley);

        Warehouse.calculateDistance(currentPickTask.getLocation(), previousPickTask.getLocation());
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromPreviousPickTask)
                .given(currentPickTask)
                .penalizesBy(34);
    }

    @Test
    void minimizeDistanceFromLastTrolleyPickTaskToPathOrigin() {
        var lastPickTask =
                createPickTask(new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 0));

        var intermediatePickTask1 = new PickTask();
        var intermediatePickTask2 = new PickTask();

        var trolley = initializeTrolley(1, 1,
                intermediatePickTask1,
                intermediatePickTask2,
                lastPickTask);

        var pathOriginLocation = new WarehouseLocation(newShelvingId(COL_A, ROW_1), Shelving.Side.LEFT, 0);
        trolley.setLocation(pathOriginLocation);
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromLastPickTaskToPathOrigin)
                .given(intermediatePickTask1,
                        intermediatePickTask2,
                        lastPickTask)
                .penalizesBy(28);
    }

    @Test
    void minimizeOrderSplitByTrolley() {
        var order1 = createOrder("order1",
                createOrderItem(1),
                createOrderItem(1),
                createOrderItem(1),
                createOrderItem(1));

        var order2 = createOrder("order2",
                createOrderItem(1),
                createOrderItem(1),
                createOrderItem(1),
                createOrderItem(1));

        var order1Trolley1 = initializeTrolley(2, 1,
                createPickTask(order1.getItems().get(0)),
                createPickTask(order1.getItems().get(1)));
        var order1Trolley2 = initializeTrolley(1, 1,
                createPickTask(order1.getItems().get(2)));
        var order1Trolley3 = initializeTrolley(1, 1,
                createPickTask(order1.getItems().get(3)));

        var order2Trolley1 = initializeTrolley(4, 1,
                createPickTask(order2.getItems().get(0)),
                createPickTask(order2.getItems().get(1)),
                createPickTask(order2.getItems().get(2)),
                createPickTask(order2.getItems().get(3)));

        var allPickTasks = Stream.of(order1Trolley1.getPickTasks(),
                order1Trolley2.getPickTasks(),
                order1Trolley3.getPickTasks(),
                order2Trolley1.getPickTasks())
                .flatMap(Collection::stream).toArray();

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeOrderSplitByTrolley)
                .given(allPickTasks)
                .penalizesBy(4 * 1000);
    }

    private static Order createOrder(String id, OrderItem... items) {
        var order = new Order();
        order.setId(id);
        for (int i = 0; i < items.length; i++) {
            OrderItem item = items[i];
            item.setOrder(order);
            item.setId(order.getId() + "_item_" + i);
            order.getItems().add(item);
        }
        return order;
    }

    private static OrderItem createOrderItem(int volume) {
        var item = new OrderItem();
        var product = new Product();
        product.setVolume(volume);
        item.setProduct(product);
        return item;
    }

    private static PickTask createPickTask(OrderItem item) {
        return new PickTask("", item);
    }

    private static PickTask createPickTask(WarehouseLocation location) {
        var item = new OrderItem();
        var product = new Product();
        product.setLocation(location);
        item.setProduct(product);
        return new PickTask("1", item);
    }

    private static Trolley initializeTrolley(int bucketCount, int bucketCapacity, PickTask... picks) {
        var trolley = new Trolley();
        trolley.setBucketCapacity(bucketCapacity);
        trolley.setBucketCount(bucketCount);
        trolley.setPickTasks(List.of(picks));

        var entities = Stream.concat(Stream.of(trolley), Arrays.stream(picks)).toArray();

        SolutionManager.updateShadowVariables(OrderPickingSolution.class, entities);

        return trolley;
    }
}
