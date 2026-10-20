package org.acme.orderpicking.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.PickingCart;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every order picking justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why an order picking constraint was matched.",
        oneOf = {
                // Hard constraints
                PickingPlanJustification.RequiredNumberOfBucketsJustification.class,
                PickingPlanJustification.ProductFitsInBucketJustification.class,

                // Soft constraints
                PickingPlanJustification.DistanceFromPreviousPickTaskJustification.class,
                PickingPlanJustification.DistanceFromLastPickTaskToPathOriginJustification.class,
                PickingPlanJustification.OrderSplitByPickingCartJustification.class
        })
public interface PickingPlanJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "A picking cart needs more buckets for its orders than it has.",
            allOf = { PickingPlanJustification.class })
    record RequiredNumberOfBucketsJustification(
            @Schema(description = "The id of the picking cart that is short of buckets.") String pickingCart,
            @Schema(description = "The number of buckets the orders on the picking cart take up.") long requiredBuckets,
            @Schema(description = "The number of buckets the picking cart has.") int availableBuckets)
            implements
                PickingPlanJustification {

        public static RequiredNumberOfBucketsJustification of(PickingCart pickingCart, long requiredBuckets) {
            return new RequiredNumberOfBucketsJustification(pickingCart.getId(), requiredBuckets, pickingCart.getBucketCount());
        }

        @Override
        public String getDescription() {
            return "Picking cart '%s' needs %d buckets for its orders, but only has %d."
                    .formatted(pickingCart, requiredBuckets, availableBuckets);
        }
    }

    @Schema(description = "A picking cart picks a product that is larger than its buckets.",
            allOf = { PickingPlanJustification.class })
    record ProductFitsInBucketJustification(
            @Schema(description = "The id of the picking cart with the undersized buckets.") String pickingCart,
            @Schema(description = "The id of the pick task of the product.") String pickTask,
            @Schema(description = "The id of the product that does not fit.") String product,
            @Schema(description = "The volume of the product in cm3.") int volume,
            @Schema(description = "The volume of one bucket of the picking cart in cm3.") int bucketCapacity)
            implements
                PickingPlanJustification {

        public static ProductFitsInBucketJustification of(PickTask pickTask) {
            PickingCart pickingCart = pickTask.getPickingCart();
            return new ProductFitsInBucketJustification(pickingCart.getId(), pickTask.getId(), pickTask.getProduct().id(),
                    pickTask.getVolume(), pickingCart.getBucketCapacity());
        }

        @Override
        public String getDescription() {
            return "Picking cart '%s' picks product '%s' of %d cm3 for pick task '%s', but its buckets only hold %d cm3."
                    .formatted(pickingCart, product, volume, pickTask, bucketCapacity);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "The distance a picking cart walks to a pick task from the previous one, or from its start location.",
            allOf = { PickingPlanJustification.class })
    record DistanceFromPreviousPickTaskJustification(
            @Schema(description = "The id of the picking cart walking the distance.") String pickingCart,
            @Schema(description = "The id of the pick task the picking cart walks to.") String pickTask,
            @Schema(description = """
                    The id of the pick task the picking cart walks from, or null if it walks from its start location.""") String previousPickTask,
            @Schema(description = "The distance walked, in meters.") int distance)
            implements
                PickingPlanJustification {

        public static DistanceFromPreviousPickTaskJustification of(PickTask pickTask, int distance) {
            PickTask previousPickTask = pickTask.getPreviousPickTask();
            return new DistanceFromPreviousPickTaskJustification(pickTask.getPickingCart().getId(), pickTask.getId(),
                    previousPickTask == null ? null : previousPickTask.getId(), distance);
        }

        @Override
        public String getDescription() {
            String from = previousPickTask == null ? "its start location" : "pick task '%s'".formatted(previousPickTask);
            return "Picking cart '%s' walks %d meters from %s to pick task '%s'."
                    .formatted(pickingCart, distance, from, pickTask);
        }
    }

    @Schema(description = "The distance a picking cart walks from its last pick task back to its start location.",
            allOf = { PickingPlanJustification.class })
    record DistanceFromLastPickTaskToPathOriginJustification(
            @Schema(description = "The id of the picking cart walking the distance.") String pickingCart,
            @Schema(description = "The id of the last pick task of the picking cart.") String pickTask,
            @Schema(description = "The distance walked, in meters.") int distance)
            implements
                PickingPlanJustification {

        public static DistanceFromLastPickTaskToPathOriginJustification of(PickTask pickTask, int distance) {
            return new DistanceFromLastPickTaskToPathOriginJustification(pickTask.getPickingCart().getId(), pickTask.getId(),
                    distance);
        }

        @Override
        public String getDescription() {
            return "Picking cart '%s' walks %d meters from its last pick task '%s' back to its start location."
                    .formatted(pickingCart, distance, pickTask);
        }
    }

    @Schema(description = "The number of picking carts an order is prepared on.",
            allOf = { PickingPlanJustification.class })
    record OrderSplitByPickingCartJustification(
            @Schema(description = "The id of the order.") String order,
            @Schema(description = "The number of picking carts that pick items of the order.") long pickingCartCount)
            implements
                PickingPlanJustification {

        @Override
        public String getDescription() {
            return "Order '%s' is prepared on %d picking cart(s).".formatted(order, pickingCartCount);
        }
    }
}
