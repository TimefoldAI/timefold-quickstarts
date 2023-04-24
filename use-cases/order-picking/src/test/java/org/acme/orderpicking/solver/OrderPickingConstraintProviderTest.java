package org.acme.orderpicking.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.TrolleyOrTrolleyStep;
import org.acme.orderpicking.domain.TrolleyStep;
import org.acme.orderpicking.domain.Warehouse;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import static org.acme.orderpicking.domain.Shelving.newShelvingId;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_A;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_C;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_D;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_E;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_2;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_3;

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

        TrolleyStep trolley1Step1 = mockTrolleyStep(order1.getItems().get(0));
        TrolleyStep trolley1Step2 = mockTrolleyStep(order1.getItems().get(1));

        TrolleyStep trolley1Step3 = mockTrolleyStep(order2.getItems().get(0));
        TrolleyStep trolley1Step4 = mockTrolleyStep(order2.getItems().get(1));

        //Trolley1:
        //Order1 total volume = 9 -> requires 2 buckets
        //Order2 total volume = 5 -> requires 1 bucket
        //Total required buckets = 3
        //Penalization = 3 - 2 = 1
        Trolley trolley1 = mockTrolley(2, 5,
                trolley1Step1,
                trolley1Step2,
                trolley1Step3,
                trolley1Step4);

        TrolleyStep trolley2Step1 = mockTrolleyStep(order1.getItems().get(2));
        TrolleyStep trolley2Step2 = mockTrolleyStep(order1.getItems().get(3));

        TrolleyStep trolley2Step3 = mockTrolleyStep(order2.getItems().get(2));
        TrolleyStep trolley2Step4 = mockTrolleyStep(order2.getItems().get(3));
        TrolleyStep trolley2Step5 = mockTrolleyStep(order2.getItems().get(4));
        TrolleyStep trolley2Step6 = mockTrolleyStep(order2.getItems().get(5));

        //Trolley2:
        //Order1 total volume = 17 -> requires 2 bucket
        //Order2 total volume = 33 -> requires 4 buckets
        //Total required buckets = 6
        //Penalization = 6 - 2 = 4
        Trolley trolley2 = mockTrolley(2, 10,
                trolley2Step1,
                trolley2Step2,
                trolley2Step3,
                trolley2Step4,
                trolley2Step5,
                trolley2Step6);

        //Penalization Trolley1 = 1
        //Penalization Trolley2 = 4
        //Total penalization = 5
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::requiredNumberOfBuckets)
                .given(trolley1Step1,
                        trolley1Step2,
                        trolley1Step3,
                        trolley1Step4,
                        trolley2Step1,
                        trolley2Step2,
                        trolley2Step3,
                        trolley2Step4,
                        trolley2Step5,
                        trolley2Step6)
                .penalizesBy(5);
    }

    @Test
    void minimizeDistanceFromPreviousTrolleyStep() {
        TrolleyStep currentTrolleyStep = mockTrolleyStep(new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 1));
        TrolleyStep previousTrolleyStep = mockTrolleyStep(new WarehouseLocation(newShelvingId(COL_E, ROW_1), Shelving.Side.RIGHT, 3));
        currentTrolleyStep.setPreviousElement(previousTrolleyStep);

        Warehouse.calculateDistance(currentTrolleyStep.getLocation(), previousTrolleyStep.getLocation());
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromPreviousTrolleyStep)
                .given(currentTrolleyStep)
                .penalizesBy(34);
    }

    @Test
    void minimizeDistanceFromLastTrolleyStepToPathOrigin() {
        TrolleyStep lastTrolleyStep = mockTrolleyStep(new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 0));

        TrolleyStep intermediateTrolleyStep1 = new TrolleyStep();
        TrolleyStep intermediateTrolleyStep2 = new TrolleyStep();

        Trolley trolley = mockTrolley(1, 1,
                intermediateTrolleyStep1,
                intermediateTrolleyStep2,
                lastTrolleyStep);

        WarehouseLocation pathOriginLocation = new WarehouseLocation(newShelvingId(COL_A, ROW_1), Shelving.Side.LEFT, 0);
        trolley.setLocation(pathOriginLocation);
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromLastTrolleyStepToPathOrigin)
                .given(intermediateTrolleyStep1,
                        intermediateTrolleyStep2,
                        lastTrolleyStep)
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

        Trolley order1Trolley1 = mockTrolley(2, 1,
                mockTrolleyStep(order1.getItems().get(0)),
                mockTrolleyStep(order1.getItems().get(1)));
        Trolley order1Trolley2 = mockTrolley(1, 1,
                mockTrolleyStep(order1.getItems().get(2)));
        Trolley order1Trolley3 = mockTrolley(1, 1,
                mockTrolleyStep(order1.getItems().get(3)));

        Trolley order2Trolley1 = mockTrolley(4, 1,
                mockTrolleyStep(order2.getItems().get(0)),
                mockTrolleyStep(order2.getItems().get(1)),
                mockTrolleyStep(order2.getItems().get(2)),
                mockTrolleyStep(order2.getItems().get(3)));

        Object[] allSteps = Stream.of(trolleySteps(order1Trolley1),
                trolleySteps(order1Trolley2),
                trolleySteps(order1Trolley3),
                trolleySteps(order2Trolley1))
                .flatMap(Collection::stream).toArray();

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeOrderSplitByTrolley)
                .given(allSteps)
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

    private static TrolleyStep mockTrolleyStep(OrderItem item) {
        return new TrolleyStep(item);
    }

    private static TrolleyStep mockTrolleyStep(WarehouseLocation location) {
        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setLocation(location);
        item.setProduct(product);
        return new TrolleyStep(item);
    }

    private static Trolley mockTrolley(int bucketCount, int bucketCapacity, TrolleyStep... steps) {
        Trolley trolley = new Trolley();
        trolley.setBucketCapacity(bucketCapacity);
        trolley.setBucketCount(bucketCount);
        linkPathElements(trolley, steps);
        return trolley;
    }

    private static List<TrolleyStep> trolleySteps(Trolley trolley) {
        ArrayList<TrolleyStep> result = new ArrayList<>();
        TrolleyStep nextElement = trolley.getNextElement();
        while (nextElement != null) {
            result.add(nextElement);
            nextElement = nextElement.getNextElement();
        }
        return result;
    }

    private static void linkPathElements(Trolley trolley, TrolleyStep... trolleySteps) {
        TrolleyOrTrolleyStep previousStep = trolley;
        for (TrolleyStep trolleyStep : trolleySteps) {
            trolleyStep.setTrolley(trolley);
            trolleyStep.setPreviousElement(previousStep);
            previousStep.setNextElement(trolleyStep);
            previousStep = trolleyStep;
        }
    }
}
