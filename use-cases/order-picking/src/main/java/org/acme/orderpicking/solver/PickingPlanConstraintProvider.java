package org.acme.orderpicking.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinct;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;
import static org.acme.orderpicking.domain.Warehouse.calculateDistance;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingPlanConstraintProperties;
import org.acme.orderpicking.domain.justification.PickingPlanJustification.DistanceFromLastPickTaskToPathOriginJustification;
import org.acme.orderpicking.domain.justification.PickingPlanJustification.DistanceFromPreviousPickTaskJustification;
import org.acme.orderpicking.domain.justification.PickingPlanJustification.OrderSplitByTrolleyJustification;
import org.acme.orderpicking.domain.justification.PickingPlanJustification.ProductFitsInBucketJustification;
import org.acme.orderpicking.domain.justification.PickingPlanJustification.RequiredNumberOfBucketsJustification;

/**
 * Constraint definitions for solving the order picking problem.
 *
 * @see PickTask for more information about the model constructed by the Solver.
 */
public class PickingPlanConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                requiredNumberOfBuckets(constraintFactory),
                productFitsInBucket(constraintFactory),

                // Soft constraints
                minimizeDistanceFromPreviousPickTask(constraintFactory),
                minimizeDistanceFromLastPickTaskToPathOrigin(constraintFactory),
                minimizeOrderSplitByTrolley(constraintFactory)
        };
    }

    /**
     * Ensure that a Trolley has a sufficient number of buckets for holding all elements picked along the path and
     * consider that buckets are not shared between orders.
     */
    Constraint requiredNumberOfBuckets(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(PickTask.class)
                //raw total volume per order
                .groupBy(PickTask::getTrolley,
                        PickTask::getOrderId,
                        sum(PickTask::getVolume))
                //required buckets per trolley, adding up the required buckets of each order
                .groupBy((trolley, orderId, orderTotalVolume) -> trolley,
                        sum((trolley, orderId, orderTotalVolume) -> trolley.calculateRequiredBuckets(orderTotalVolume)))
                //penalization if the trolley doesn't have enough buckets to hold the orders
                .filter((trolley, trolleyTotalBuckets) -> trolley.getBucketCount() < trolleyTotalBuckets)
                .penalize(HardSoftScore.ONE_HARD,
                        (trolley, trolleyTotalBuckets) -> trolleyTotalBuckets - trolley.getBucketCount())
                .justifyWith((trolley, trolleyTotalBuckets, score) -> RequiredNumberOfBucketsJustification.of(trolley,
                        trolleyTotalBuckets))
                .asConstraint(new ConstraintInfo(PickingPlanConstraintProperties.REQUIRED_NUMBER_OF_BUCKETS,
                        PickingPlanConstraintProperties.REQUIRED_NUMBER_OF_BUCKETS,
                        "A trolley must have enough buckets for all its orders, as a bucket is never shared between two orders.",
                        PickingPlanConstraintGroup.BUCKET_CAPACITY));
    }

    /**
     * Ensure that a product is only picked by a Trolley whose buckets are large enough to hold it, as a product is
     * indivisible and can't be spread over multiple buckets.
     */
    Constraint productFitsInBucket(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .filter(pickTask -> pickTask.getVolume() > pickTask.getTrolley().getBucketCapacity())
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((pickTask, score) -> ProductFitsInBucketJustification.of(pickTask))
                .asConstraint(new ConstraintInfo(PickingPlanConstraintProperties.PRODUCT_FITS_IN_BUCKET,
                        PickingPlanConstraintProperties.PRODUCT_FITS_IN_BUCKET,
                        "A trolley must not pick a product that is larger than its buckets.",
                        PickingPlanConstraintGroup.BUCKET_CAPACITY));
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance with the previous element in the
     * list is as short as possible.
     */
    Constraint minimizeDistanceFromPreviousPickTask(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .penalize(HardSoftScore.ONE_SOFT,
                        pickTask -> calculateDistance(pickTask.getPreviousLocation(), pickTask.getLocation()))
                .justifyWith((pickTask, score) -> DistanceFromPreviousPickTaskJustification.of(pickTask,
                        calculateDistance(pickTask.getPreviousLocation(), pickTask.getLocation())))
                .asConstraint(new ConstraintInfo(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK,
                        PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK,
                        "Minimize the travel distance between consecutive pick tasks of a trolley.",
                        PickingPlanConstraintGroup.TRAVEL_DISTANCE));
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance of the last element in the list
     * with the return point (the Trolley location) is as short as possible.
     */
    Constraint minimizeDistanceFromLastPickTaskToPathOrigin(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .filter(PickTask::isLast)
                .penalize(HardSoftScore.ONE_SOFT,
                        pickTask -> calculateDistance(pickTask.getLocation(), pickTask.getTrolley().getLocation()))
                .justifyWith((pickTask, score) -> DistanceFromLastPickTaskToPathOriginJustification.of(pickTask,
                        calculateDistance(pickTask.getLocation(), pickTask.getTrolley().getLocation())))
                .asConstraint(new ConstraintInfo(
                        PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_LAST_PICK_TASK_TO_PATH_ORIGIN,
                        PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_LAST_PICK_TASK_TO_PATH_ORIGIN,
                        "Minimize the return distance from the last pick task of a trolley to its start location.",
                        PickingPlanConstraintGroup.TRAVEL_DISTANCE));
    }

    /**
     * An Order should ideally be prepared on the same trolley, penalize the order splitting into different trolleys.
     */
    Constraint minimizeOrderSplitByTrolley(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .groupBy(PickTask::getOrderId,
                        countDistinct(PickTask::getTrolley))
                .penalize(HardSoftScore.ONE_SOFT,
                        (orderId, trolleySpreadCount) -> trolleySpreadCount * 1000)
                .justifyWith((orderId, trolleySpreadCount, score) -> new OrderSplitByTrolleyJustification(orderId,
                        trolleySpreadCount))
                .asConstraint(new ConstraintInfo(PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_TROLLEY,
                        PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_TROLLEY,
                        "Minimize the number of trolleys used for each order.",
                        PickingPlanConstraintGroup.ORDER_COHESION));
    }
}
