package org.acme.orderpicking.solver;

import org.acme.orderpicking.domain.TrolleyStep;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import static org.acme.orderpicking.domain.Warehouse.calculateDistance;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinctLong;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

/**
 * Constraint definitions for solving the order picking problem.
 * 
 * @see TrolleyStep for more information about the model constructed by the Solver.
 * @see ConstraintProvider
 * @see ConstraintFactory
 */
public class OrderPickingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredNumberOfBuckets(constraintFactory),
                minimizeDistanceFromPreviousTrolleyStep(constraintFactory),
                minimizeDistanceFromLastTrolleyStepToPathOrigin(constraintFactory),
                minimizeOrderSplitByTrolley(constraintFactory)
        };
    }

    /**
     * Ensure that a Trolley has a sufficient number of buckets for holding all elements picked along the path and
     * consider that buckets are not shared between orders.
     */
    Constraint requiredNumberOfBuckets(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(TrolleyStep.class)
                //raw total volume per order
                .groupBy(trolleyStep -> trolleyStep.getTrolley(),
                        trolleyStep -> trolleyStep.getOrderItem().getOrder(),
                        sum(trolleyStep -> trolleyStep.getOrderItem().getVolume()))
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
        return constraintFactory.forEach(TrolleyStep.class)
                .groupBy(trolleyStep -> trolleyStep.getOrderItem().getOrder(),
                        countDistinctLong(TrolleyStep::getTrolley))
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        (order, trolleySpreadCount) -> trolleySpreadCount * 1000)
                .asConstraint("Minimize order split by trolley");
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance with the previous element in the
     * chain is as short as possible.
     * 
     * @see TrolleyStep for more information about the model constructed by the Solver.
     */
    Constraint minimizeDistanceFromPreviousTrolleyStep(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TrolleyStep.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        trolleyStep -> calculateDistance(trolleyStep.getPreviousElement().getLocation(), trolleyStep.getLocation()))
                .asConstraint("Minimize the distance from the previous trolley step");
    }

    /**
     * Minimize the distance travelled by the trolley by ensuring that the distance of the last element in the chain
     * with the return point (the Trolley location) is as short as possible.
     *
     * @see TrolleyStep for more information about the model constructed by the Solver.
     */
    Constraint minimizeDistanceFromLastTrolleyStepToPathOrigin(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TrolleyStep.class)
                .filter(TrolleyStep::isLast)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        trolleyStep -> calculateDistance(trolleyStep.getLocation(), trolleyStep.getTrolley().getLocation()))
                .asConstraint("Minimize the distance from last trolley step to the path origin");
    }

    private int calculateOrderRequiredBuckets(int orderVolume, int bucketVolume) {
        return (orderVolume + (bucketVolume - 1)) / bucketVolume;
    }
}
