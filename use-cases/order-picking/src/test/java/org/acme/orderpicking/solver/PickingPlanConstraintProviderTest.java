package org.acme.orderpicking.solver;

import static org.acme.orderpicking.support.TestHelper.location;
import static org.acme.orderpicking.support.TestHelper.pickTask;
import static org.acme.orderpicking.support.TestHelper.pickingCart;

import java.util.Collection;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingCart;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.RackSide;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PickingPlanConstraintProviderTest {

    @Inject
    ConstraintVerifier<PickingPlanConstraintProvider, PickingPlan> constraintVerifier;

    @Test
    void requiredNumberOfBuckets() {
        PickTask pickingCart1PickTask1 = pickTask("1-0", "1", 4);
        PickTask pickingCart1PickTask2 = pickTask("1-1", "1", 5);
        PickTask pickingCart1PickTask3 = pickTask("2-0", "2", 4);
        PickTask pickingCart1PickTask4 = pickTask("2-1", "2", 1);
        //PickingCart1:
        //Order1 total volume = 9 -> requires 2 buckets
        //Order2 total volume = 5 -> requires 1 bucket
        //Total required buckets = 3
        //Penalization = 3 - 2 = 1
        pickingCart("c1", 2, 5, pickingCart1PickTask1, pickingCart1PickTask2, pickingCart1PickTask3, pickingCart1PickTask4);

        PickTask pickingCart2PickTask1 = pickTask("1-2", "1", 9);
        PickTask pickingCart2PickTask2 = pickTask("1-3", "1", 8);
        PickTask pickingCart2PickTask3 = pickTask("2-2", "2", 8);
        PickTask pickingCart2PickTask4 = pickTask("2-3", "2", 6);
        PickTask pickingCart2PickTask5 = pickTask("2-4", "2", 10);
        PickTask pickingCart2PickTask6 = pickTask("2-5", "2", 9);
        //PickingCart2:
        //Order1 total volume = 17 -> requires 2 buckets
        //Order2 total volume = 33 -> requires 4 buckets
        //Total required buckets = 6
        //Penalization = 6 - 2 = 4
        pickingCart("c2", 2, 10, pickingCart2PickTask1, pickingCart2PickTask2, pickingCart2PickTask3, pickingCart2PickTask4,
                pickingCart2PickTask5, pickingCart2PickTask6);

        //Penalization PickingCart1 = 1
        //Penalization PickingCart2 = 4
        //Total penalization = 5
        constraintVerifier.verifyThat(PickingPlanConstraintProvider::requiredNumberOfBuckets)
                .given(pickingCart1PickTask1, pickingCart1PickTask2, pickingCart1PickTask3, pickingCart1PickTask4,
                        pickingCart2PickTask1, pickingCart2PickTask2, pickingCart2PickTask3, pickingCart2PickTask4,
                        pickingCart2PickTask5, pickingCart2PickTask6)
                .penalizesBy(5);
    }

    @Test
    void requiredNumberOfBucketsWithEnoughBuckets() {
        PickTask pickTask1 = pickTask("1-0", "1", 5);
        PickTask pickTask2 = pickTask("2-0", "2", 5);
        pickingCart("c1", 2, 5, pickTask1, pickTask2);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::requiredNumberOfBuckets)
                .given(pickTask1, pickTask2)
                .penalizesBy(0);
    }

    @Test
    void productFitsInBucket() {
        PickTask fittingPickTask = pickTask("1-0", "1", 5);
        PickTask oversizedPickTask1 = pickTask("1-1", "1", 6);
        PickTask oversizedPickTask2 = pickTask("2-0", "2", 20);
        pickingCart("c1", 10, 5, fittingPickTask, oversizedPickTask1, oversizedPickTask2);

        // The same products fit in the larger buckets of the second picking cart.
        PickTask largeBucketPickTask = pickTask("3-0", "3", 20);
        pickingCart("c2", 10, 20, largeBucketPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::productFitsInBucket)
                .given(fittingPickTask, oversizedPickTask1, oversizedPickTask2, largeBucketPickTask)
                .penalizesBy(2);
    }

    @Test
    void minimizeDistanceFromPreviousPickTask() {
        PickTask previousPickTask = pickTask("1", location("E1", RackSide.RIGHT, 3));
        PickTask currentPickTask = pickTask("2", location("C3", RackSide.RIGHT, 1));
        pickingCart("c1", 1, 1, previousPickTask, currentPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeDistanceFromPreviousPickTask)
                .given(currentPickTask)
                .penalizesBy(34);
    }

    @Test
    void minimizeDistanceFromPreviousPickTaskStartsAtThePickingCartLocation() {
        PickTask firstPickTask = pickTask("1", location("A1", RackSide.LEFT, 7));
        pickingCart("c1", 1, 1, location("A1", RackSide.LEFT, 0), firstPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeDistanceFromPreviousPickTask)
                .given(firstPickTask)
                .penalizesBy(7);
    }

    @Test
    void minimizeDistanceFromLastPickTaskToPathOrigin() {
        PickTask intermediatePickTask1 = pickTask("1", location("B1", RackSide.LEFT, 4));
        PickTask intermediatePickTask2 = pickTask("2", location("C2", RackSide.RIGHT, 6));
        PickTask lastPickTask = pickTask("3", location("D2", RackSide.LEFT, 0));
        pickingCart("c1", 1, 1, location("A1", RackSide.LEFT, 0), intermediatePickTask1, intermediatePickTask2,
                lastPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeDistanceFromLastPickTaskToPathOrigin)
                .given(intermediatePickTask1, intermediatePickTask2, lastPickTask)
                .penalizesBy(28);
    }

    @Test
    void minimizeOrderSplitByPickingCart() {
        PickingCart order1PickingCart1 = pickingCart("c1", 2, 1, pickTask("1-0", "1", 1), pickTask("1-1", "1", 1));
        PickingCart order1PickingCart2 = pickingCart("c2", 1, 1, pickTask("1-2", "1", 1));
        PickingCart order1PickingCart3 = pickingCart("c3", 1, 1, pickTask("1-3", "1", 1));
        PickingCart order2PickingCart1 = pickingCart("c4", 4, 1, pickTask("2-0", "2", 1), pickTask("2-1", "2", 1),
                pickTask("2-2", "2", 1), pickTask("2-3", "2", 1));

        Object[] allPickTasks = Stream.of(order1PickingCart1, order1PickingCart2, order1PickingCart3, order2PickingCart1)
                .map(PickingCart::getPickTasks)
                .flatMap(Collection::stream)
                .toArray();

        // Order 1 is spread over 3 picking carts, order 2 over 1.
        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeOrderSplitByPickingCart)
                .given(allPickTasks)
                .penalizesBy(4 * 1000);
    }
}
