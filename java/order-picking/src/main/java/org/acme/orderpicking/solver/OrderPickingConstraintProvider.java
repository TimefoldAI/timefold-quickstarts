package org.acme.orderpicking.solver;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.WarehouseLocation;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinct;
import static org.acme.orderpicking.domain.Warehouse.calculateDistance;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinctLong;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

/**
 * Constraint definitions for solving the order picking problem.
 * 
 * @see PickTask for more information about the model constructed by the Solver.
 * @see ConstraintProvider
 * @see ConstraintFactory
 */
public class OrderPickingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard
                requiredNumberOfBuckets(constraintFactory),

                // Soft
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
                        pick -> pick.getOrderItem().getOrder(),
                        sum(pick -> pick.getOrderItem().getVolume()))
                //required buckets per order
                .groupBy((trolley, order, orderTotalVolume) -> trolley,
                        (trolley, order, orderTotalVolume) -> order,
                        sum((trolley, order, orderTotalVolume) -> calculateOrderRequiredBuckets(orderTotalVolume, trolley.getBucketCapacity())))
                //required buckets per trolley
                .groupBy((trolley, order, orderTotalBuckets) -> trolley,
                        sum((trolley, order, orderTotalBuckets) -> orderTotalBuckets))
                //penalization if the trolley don't have enough buckets to hold the orders
                .filter((trolley, trolleyTotalBuckets) -> trolley.getBucketCount() < trolleyTotalBuckets)
                .penalize(HardSoftLongScore.ONE_HARD,
                        (trolley, trolleyTotalBuckets) -> trolleyTotalBuckets - trolley.getBucketCount())
                .asConstraint("Required number of buckets");
    }

    /**
     * An Order should ideally be prepared on the same trolley, penalize the order splitting into different trolleys.
     */
    Constraint minimizeOrderSplitByTrolley(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .groupBy(pick -> pick.getOrderItem().getOrder(),
                        countDistinct(PickTask::getTrolley))
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        (order, trolleySpreadCount) -> trolleySpreadCount * 1000)
                .asConstraint("Minimize order split by trolley");
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance with the previous element in the
     * chain is as short as possible.
     * 
     * @see PickTask for more information about the model constructed by the Solver.
     */
    Constraint minimizeDistanceFromPreviousPickTask(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PickTask.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        pick -> {
                            var previousLocation = pick.getPreviousPickTask() != null
                                    ? pick.getPreviousPickTask().getLocation()
                                    : pick.getTrolley().getLocation();
                            return calculateDistance(previousLocation, pick.getLocation());
                        })
                .asConstraint("Minimize the distance from the previous trolley pick");
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
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        pick -> calculateDistance(pick.getLocation(), pick.getTrolley().getLocation()))
                .asConstraint("Minimize the distance from last trolley pick to the path origin");
    }

    private int calculateOrderRequiredBuckets(int orderVolume, int bucketVolume) {
        return (orderVolume + (bucketVolume - 1)) / bucketVolume;
    }
}
