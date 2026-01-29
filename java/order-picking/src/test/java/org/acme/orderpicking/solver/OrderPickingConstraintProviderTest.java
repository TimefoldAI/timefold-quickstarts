package org.acme.orderpicking.solver;

import static org.acme.orderpicking.domain.Shelving.newShelvingId;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_A;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_C;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_D;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_E;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_2;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_3;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.solver.SolutionManager;
import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.Pick;
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
        Order order1 = mockOrder("order1",
                mockOrderItem(4), //goes in Trolley1
                mockOrderItem(5), //goes in Trolley1
                mockOrderItem(9), //goes in Trolley2
                mockOrderItem(8)); //goes in Trolley2

        Order order2 = mockOrder("order2",
                mockOrderItem(4), //goes in Trolley1
                mockOrderItem(1), //goes in Trolley1
                mockOrderItem(8), //goes in Trolley2
                mockOrderItem(6), //goes in Trolley2
                mockOrderItem(10), //goes in Trolley2
                mockOrderItem(9)); //goes in Trolley2

        Pick trolley1Pick1 = mockPick(order1.getItems().get(0));
        Pick trolley1Pick2 = mockPick(order1.getItems().get(1));

        Pick trolley1Pick3 = mockPick(order2.getItems().get(0));
        Pick trolley1Pick4 = mockPick(order2.getItems().get(1));

        //Trolley1:
        //Order1 total volume = 9 -> requires 2 buckets
        //Order2 total volume = 5 -> requires 1 bucket
        //Total required buckets = 3
        //Penalization = 3 - 2 = 1
        Trolley trolley1 = initializeTrolley(2, 5,
                trolley1Pick1,
                trolley1Pick2,
                trolley1Pick3,
                trolley1Pick4);

        Pick trolley2Pick1 = mockPick(order1.getItems().get(2));
        Pick trolley2Pick2 = mockPick(order1.getItems().get(3));

        Pick trolley2Pick3 = mockPick(order2.getItems().get(2));
        Pick trolley2Pick4 = mockPick(order2.getItems().get(3));
        Pick trolley2Pick5 = mockPick(order2.getItems().get(4));
        Pick trolley2Pick6 = mockPick(order2.getItems().get(5));

        //Trolley2:
        //Order1 total volume = 17 -> requires 2 bucket
        //Order2 total volume = 33 -> requires 4 buckets
        //Total required buckets = 6
        //Penalization = 6 - 2 = 4
        Trolley trolley2 = initializeTrolley(2, 10,
                trolley2Pick1,
                trolley2Pick2,
                trolley2Pick3,
                trolley2Pick4,
                trolley2Pick5,
                trolley2Pick6);

        //Penalization Trolley1 = 1
        //Penalization Trolley2 = 4
        //Total penalization = 5
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::requiredNumberOfBuckets)
                .given(trolley1Pick1,
                        trolley1Pick2,
                        trolley1Pick3,
                        trolley1Pick4,
                        trolley2Pick1,
                        trolley2Pick2,
                        trolley2Pick3,
                        trolley2Pick4,
                        trolley2Pick5,
                        trolley2Pick6)
                .penalizesBy(5);
    }

    @Test
    void minimizeDistanceFromPreviousPick() {
        Pick currentPick =
                mockPick(new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 1));
        Pick previousPick =
                mockPick(new WarehouseLocation(newShelvingId(COL_E, ROW_1), Shelving.Side.RIGHT, 3));
        currentPick.setPreviousPick(previousPick);

        Trolley trolley = initializeTrolley(1, 1,
                previousPick,
                currentPick);
        currentPick.setTrolley(trolley);

        Warehouse.calculateDistance(currentPick.getLocation(), previousPick.getLocation());
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromPreviousPick)
                .given(currentPick)
                .penalizesBy(34);
    }

    @Test
    void minimizeDistanceFromLastTrolleyPickToPathOrigin() {
        Pick lastPick =
                mockPick(new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 0));

        Pick intermediatePick1 = new Pick();
        Pick intermediatePick2 = new Pick();

        Trolley trolley = initializeTrolley(1, 1,
                intermediatePick1,
                intermediatePick2,
                lastPick);

        WarehouseLocation pathOriginLocation = new WarehouseLocation(newShelvingId(COL_A, ROW_1), Shelving.Side.LEFT, 0);
        trolley.setLocation(pathOriginLocation);
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromLastPickToPathOrigin)
                .given(intermediatePick1,
                        intermediatePick2,
                        lastPick)
                .penalizesBy(28);
    }

    @Test
    void minimizeOrderSplitByTrolley() {
        Order order1 = mockOrder("order1",
                mockOrderItem(1),
                mockOrderItem(1),
                mockOrderItem(1),
                mockOrderItem(1));

        Order order2 = mockOrder("order2",
                mockOrderItem(1),
                mockOrderItem(1),
                mockOrderItem(1),
                mockOrderItem(1));

        Trolley order1Trolley1 = initializeTrolley(2, 1,
                mockPick(order1.getItems().get(0)),
                mockPick(order1.getItems().get(1)));
        Trolley order1Trolley2 = initializeTrolley(1, 1,
                mockPick(order1.getItems().get(2)));
        Trolley order1Trolley3 = initializeTrolley(1, 1,
                mockPick(order1.getItems().get(3)));

        Trolley order2Trolley1 = initializeTrolley(4, 1,
                mockPick(order2.getItems().get(0)),
                mockPick(order2.getItems().get(1)),
                mockPick(order2.getItems().get(2)),
                mockPick(order2.getItems().get(3)));

        Object[] allPicks = Stream.of(order1Trolley1.getPicks(),
                order1Trolley2.getPicks(),
                order1Trolley3.getPicks(),
                order2Trolley1.getPicks())
                .flatMap(Collection::stream).toArray();

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeOrderSplitByTrolley)
                .given(allPicks)
                .penalizesBy(4 * 1000);
    }

    private static Order mockOrder(String id, OrderItem... items) {
        Order order = new Order();
        order.setId(id);
        for (int i = 0; i < items.length; i++) {
            OrderItem item = items[i];
            item.setOrder(order);
            item.setId(order.getId() + "_item_" + i);
            order.getItems().add(item);
        }
        return order;
    }

    private static OrderItem mockOrderItem(int volume) {
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setVolume(volume);
        item.setProduct(product);
        return item;
    }

    private static Pick mockPick(OrderItem item) {
        return new Pick("", item);
    }

    private static Pick mockPick(WarehouseLocation location) {
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setLocation(location);
        item.setProduct(product);
        return new Pick("1", item);
    }

    private static Trolley initializeTrolley(int bucketCount, int bucketCapacity, Pick... picks) {
        Trolley trolley = new Trolley();
        trolley.setBucketCapacity(bucketCapacity);
        trolley.setBucketCount(bucketCount);
        trolley.setPicks(List.of(picks));

        Object[] entities = new Object[picks.length + 1];
        entities[0] = trolley;
        System.arraycopy(picks, 0, entities, 1, picks.length);

        SolutionManager.updateShadowVariables(OrderPickingSolution.class, entities);

        return trolley;
    }
}
