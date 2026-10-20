package org.acme.orderpicking.solver;

import static org.acme.orderpicking.support.TestHelper.location;
import static org.acme.orderpicking.support.TestHelper.pickTask;
import static org.acme.orderpicking.support.TestHelper.trolley;

import java.util.Collection;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingPlan;
import org.acme.orderpicking.domain.ShelvingSide;
import org.acme.orderpicking.domain.Trolley;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PickingPlanConstraintProviderTest {

    @Inject
    ConstraintVerifier<PickingPlanConstraintProvider, PickingPlan> constraintVerifier;

    @Test
    void requiredNumberOfBuckets() {
        PickTask trolley1PickTask1 = pickTask("1-0", "1", 4);
        PickTask trolley1PickTask2 = pickTask("1-1", "1", 5);
        PickTask trolley1PickTask3 = pickTask("2-0", "2", 4);
        PickTask trolley1PickTask4 = pickTask("2-1", "2", 1);
        //Trolley1:
        //Order1 total volume = 9 -> requires 2 buckets
        //Order2 total volume = 5 -> requires 1 bucket
        //Total required buckets = 3
        //Penalization = 3 - 2 = 1
        trolley("t1", 2, 5, trolley1PickTask1, trolley1PickTask2, trolley1PickTask3, trolley1PickTask4);

        PickTask trolley2PickTask1 = pickTask("1-2", "1", 9);
        PickTask trolley2PickTask2 = pickTask("1-3", "1", 8);
        PickTask trolley2PickTask3 = pickTask("2-2", "2", 8);
        PickTask trolley2PickTask4 = pickTask("2-3", "2", 6);
        PickTask trolley2PickTask5 = pickTask("2-4", "2", 10);
        PickTask trolley2PickTask6 = pickTask("2-5", "2", 9);
        //Trolley2:
        //Order1 total volume = 17 -> requires 2 buckets
        //Order2 total volume = 33 -> requires 4 buckets
        //Total required buckets = 6
        //Penalization = 6 - 2 = 4
        trolley("t2", 2, 10, trolley2PickTask1, trolley2PickTask2, trolley2PickTask3, trolley2PickTask4,
                trolley2PickTask5, trolley2PickTask6);

        //Penalization Trolley1 = 1
        //Penalization Trolley2 = 4
        //Total penalization = 5
        constraintVerifier.verifyThat(PickingPlanConstraintProvider::requiredNumberOfBuckets)
                .given(trolley1PickTask1, trolley1PickTask2, trolley1PickTask3, trolley1PickTask4,
                        trolley2PickTask1, trolley2PickTask2, trolley2PickTask3, trolley2PickTask4,
                        trolley2PickTask5, trolley2PickTask6)
                .penalizesBy(5);
    }

    @Test
    void requiredNumberOfBucketsWithEnoughBuckets() {
        PickTask pickTask1 = pickTask("1-0", "1", 5);
        PickTask pickTask2 = pickTask("2-0", "2", 5);
        trolley("t1", 2, 5, pickTask1, pickTask2);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::requiredNumberOfBuckets)
                .given(pickTask1, pickTask2)
                .penalizesBy(0);
    }

    @Test
    void productFitsInBucket() {
        PickTask fittingPickTask = pickTask("1-0", "1", 5);
        PickTask oversizedPickTask1 = pickTask("1-1", "1", 6);
        PickTask oversizedPickTask2 = pickTask("2-0", "2", 20);
        trolley("t1", 10, 5, fittingPickTask, oversizedPickTask1, oversizedPickTask2);

        // The same products fit in the larger buckets of the second trolley.
        PickTask largeBucketPickTask = pickTask("3-0", "3", 20);
        trolley("t2", 10, 20, largeBucketPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::productFitsInBucket)
                .given(fittingPickTask, oversizedPickTask1, oversizedPickTask2, largeBucketPickTask)
                .penalizesBy(2);
    }

    @Test
    void minimizeDistanceFromPreviousPickTask() {
        PickTask previousPickTask = pickTask("1", location("E1", ShelvingSide.RIGHT, 3));
        PickTask currentPickTask = pickTask("2", location("C3", ShelvingSide.RIGHT, 1));
        trolley("t1", 1, 1, previousPickTask, currentPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeDistanceFromPreviousPickTask)
                .given(currentPickTask)
                .penalizesBy(34);
    }

    @Test
    void minimizeDistanceFromPreviousPickTaskStartsAtTheTrolleyLocation() {
        PickTask firstPickTask = pickTask("1", location("A1", ShelvingSide.LEFT, 7));
        trolley("t1", 1, 1, location("A1", ShelvingSide.LEFT, 0), firstPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeDistanceFromPreviousPickTask)
                .given(firstPickTask)
                .penalizesBy(7);
    }

    @Test
    void minimizeDistanceFromLastPickTaskToPathOrigin() {
        PickTask intermediatePickTask1 = pickTask("1", location("B1", ShelvingSide.LEFT, 4));
        PickTask intermediatePickTask2 = pickTask("2", location("C2", ShelvingSide.RIGHT, 6));
        PickTask lastPickTask = pickTask("3", location("D2", ShelvingSide.LEFT, 0));
        trolley("t1", 1, 1, location("A1", ShelvingSide.LEFT, 0), intermediatePickTask1, intermediatePickTask2,
                lastPickTask);

        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeDistanceFromLastPickTaskToPathOrigin)
                .given(intermediatePickTask1, intermediatePickTask2, lastPickTask)
                .penalizesBy(28);
    }

    @Test
    void minimizeOrderSplitByTrolley() {
        Trolley order1Trolley1 = trolley("t1", 2, 1, pickTask("1-0", "1", 1), pickTask("1-1", "1", 1));
        Trolley order1Trolley2 = trolley("t2", 1, 1, pickTask("1-2", "1", 1));
        Trolley order1Trolley3 = trolley("t3", 1, 1, pickTask("1-3", "1", 1));
        Trolley order2Trolley1 = trolley("t4", 4, 1, pickTask("2-0", "2", 1), pickTask("2-1", "2", 1),
                pickTask("2-2", "2", 1), pickTask("2-3", "2", 1));

        Object[] allPickTasks = Stream.of(order1Trolley1, order1Trolley2, order1Trolley3, order2Trolley1)
                .map(Trolley::getPickTasks)
                .flatMap(Collection::stream)
                .toArray();

        // Order 1 is spread over 3 trolleys, order 2 over 1.
        constraintVerifier.verifyThat(PickingPlanConstraintProvider::minimizeOrderSplitByTrolley)
                .given(allPickTasks)
                .penalizesBy(4 * 1000);
    }
}
