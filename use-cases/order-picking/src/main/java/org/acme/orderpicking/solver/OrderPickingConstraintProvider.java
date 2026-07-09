package org.acme.orderpicking.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinct;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;
import static org.acme.orderpicking.domain.Warehouse.calculateDistance;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.orderpicking.domain.PickTask;

/**
 * Constraint definitions for solving the order picking problem.
 *
 * @see PickTask for more information about the model constructed by the Solver.
 */
public class OrderPickingConstraintProvider implements ConstraintProvider {

    public static final String REQUIRED_NUMBER_OF_BUCKETS = "Required number of buckets";
    public static final String MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK = "Minimize the distance from the previous trolley pick";
    public static final String MINIMIZE_DISTANCE_TO_PATH_ORIGIN =
            "Minimize the distance from last trolley pick to the path origin";
    public static final String MINIMIZE_ORDER_SPLIT_BY_TROLLEY = "Minimize order split by trolley";

    private static final int ORDER_SPLIT_PENALTY = 1000;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                requiredNumberOfBuckets(constraintFactory),

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
                // raw total volume per order
                .groupBy(PickTask::getTrolley,
                        pick -> pick.getOrderItem().getOrder(),
                        sum(pick -> pick.getOrderItem().getVolume()))
                // required buckets per order
                .groupBy((trolley, order, orderTotalVolume) -> trolley,
                        (trolley, order, orderTotalVolume) -> order,
                        sum((trolley, order, orderTotalVolume) -> calculateOrderRequiredBuckets(orderTotalVolume,
                                trolley.getBucketCapacity())))
                // required buckets per trolley
                .groupBy((trolley, order, orderTotalBuckets) -> trolley,
                        sum((trolley, order, orderTotalBuckets) -> orderTotalBuckets))
                // penalization if the trolley don't have enough buckets to hold the orders
                .filter((trolley, trolleyTotalBuckets) -> trolley.getBucketCount() < trolleyTotalBuckets)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (trolley, trolleyTotalBuckets) -> trolleyTotalBuckets - trolley.getBucketCount())
                .asConstraint(new ConstraintInfo(REQUIRED_NUMBER_OF_BUCKETS, REQUIRED_NUMBER_OF_BUCKETS,
                        "A trolley must have enough buckets to hold all order items assigned to it.",
                        OrderPickingConstraintGroup.BUCKET_CAPACITY));
    }

    /**
     * An Order should ideally be prepared on the same trolley, penalize the order splitting into different trolleys.
     */
    Constraint minimizeOrderSplitByTrolley(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .groupBy(pick -> pick.getOrderItem().getOrder(),
                        countDistinct(PickTask::getTrolley))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (order, trolleySpreadCount) -> trolleySpreadCount * ORDER_SPLIT_PENALTY)
                .asConstraint(new ConstraintInfo(MINIMIZE_ORDER_SPLIT_BY_TROLLEY, MINIMIZE_ORDER_SPLIT_BY_TROLLEY,
                        "An order should ideally be picked by a single trolley.",
                        OrderPickingConstraintGroup.ORDER_INTEGRITY));
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance with the previous element in the
     * chain is as short as possible.
     *
     * @see PickTask for more information about the model constructed by the Solver.
     */
    Constraint minimizeDistanceFromPreviousPickTask(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        pick -> {
                            var previousLocation = pick.getPreviousPickTask() != null
                                    ? pick.getPreviousPickTask().getLocation()
                                    : pick.getTrolley().getLocation();
                            return calculateDistance(previousLocation, pick.getLocation());
                        })
                .asConstraint(new ConstraintInfo(MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK, MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK,
                        "Minimize the distance travelled between consecutive picks of a trolley.",
                        OrderPickingConstraintGroup.TRAVEL_EFFICIENCY));
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance of the last element in the chain
     * with the return point (the Trolley location) is as short as possible.
     *
     * @see PickTask for more information about the model constructed by the Solver.
     */
    Constraint minimizeDistanceFromLastPickTaskToPathOrigin(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .filter(PickTask::isLast)
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        pick -> calculateDistance(pick.getLocation(), pick.getTrolley().getLocation()))
                .asConstraint(new ConstraintInfo(MINIMIZE_DISTANCE_TO_PATH_ORIGIN, MINIMIZE_DISTANCE_TO_PATH_ORIGIN,
                        "Minimize the distance travelled by a trolley returning to its origin after the last pick.",
                        OrderPickingConstraintGroup.TRAVEL_EFFICIENCY));
    }

    private static long calculateOrderRequiredBuckets(long orderVolume, long bucketVolume) {
        return (orderVolume + bucketVolume - 1) / bucketVolume;
    }
}
