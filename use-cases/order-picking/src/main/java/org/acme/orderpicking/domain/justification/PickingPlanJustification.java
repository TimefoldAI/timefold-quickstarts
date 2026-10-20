package org.acme.orderpicking.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.orderpicking.domain.PickTask;
import org.acme.orderpicking.domain.Trolley;
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

                // Soft constraints
                PickingPlanJustification.DistanceFromPreviousPickTaskJustification.class,
                PickingPlanJustification.DistanceFromLastPickTaskToPathOriginJustification.class,
                PickingPlanJustification.OrderSplitByTrolleyJustification.class
        })
public interface PickingPlanJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "A trolley needs more buckets for its orders than it has.",
            allOf = { PickingPlanJustification.class })
    record RequiredNumberOfBucketsJustification(
            @Schema(description = "The id of the trolley that is short of buckets.") String trolley,
            @Schema(description = "The number of buckets the orders on the trolley take up.") long requiredBuckets,
            @Schema(description = "The number of buckets the trolley has.") int availableBuckets)
            implements
                PickingPlanJustification {

        public static RequiredNumberOfBucketsJustification of(Trolley trolley, long requiredBuckets) {
            return new RequiredNumberOfBucketsJustification(trolley.getId(), requiredBuckets, trolley.getBucketCount());
        }

        @Override
        public String getDescription() {
            return "Trolley '%s' needs %d buckets for its orders, but only has %d."
                    .formatted(trolley, requiredBuckets, availableBuckets);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "The distance a trolley walks to a pick task from the previous one, or from its start location.",
            allOf = { PickingPlanJustification.class })
    record DistanceFromPreviousPickTaskJustification(
            @Schema(description = "The id of the trolley walking the distance.") String trolley,
            @Schema(description = "The id of the pick task the trolley walks to.") String pickTask,
            @Schema(description = """
                    The id of the pick task the trolley walks from, or null if it walks from its start location.""") String previousPickTask,
            @Schema(description = "The distance walked, in meters.") int distance)
            implements
                PickingPlanJustification {

        public static DistanceFromPreviousPickTaskJustification of(PickTask pickTask, int distance) {
            PickTask previousPickTask = pickTask.getPreviousPickTask();
            return new DistanceFromPreviousPickTaskJustification(pickTask.getTrolley().getId(), pickTask.getId(),
                    previousPickTask == null ? null : previousPickTask.getId(), distance);
        }

        @Override
        public String getDescription() {
            String from = previousPickTask == null ? "its start location" : "pick task '%s'".formatted(previousPickTask);
            return "Trolley '%s' walks %d meters from %s to pick task '%s'."
                    .formatted(trolley, distance, from, pickTask);
        }
    }

    @Schema(description = "The distance a trolley walks from its last pick task back to its start location.",
            allOf = { PickingPlanJustification.class })
    record DistanceFromLastPickTaskToPathOriginJustification(
            @Schema(description = "The id of the trolley walking the distance.") String trolley,
            @Schema(description = "The id of the last pick task of the trolley.") String pickTask,
            @Schema(description = "The distance walked, in meters.") int distance)
            implements
                PickingPlanJustification {

        public static DistanceFromLastPickTaskToPathOriginJustification of(PickTask pickTask, int distance) {
            return new DistanceFromLastPickTaskToPathOriginJustification(pickTask.getTrolley().getId(), pickTask.getId(),
                    distance);
        }

        @Override
        public String getDescription() {
            return "Trolley '%s' walks %d meters from its last pick task '%s' back to its start location."
                    .formatted(trolley, distance, pickTask);
        }
    }

    @Schema(description = "The number of trolleys an order is prepared on.",
            allOf = { PickingPlanJustification.class })
    record OrderSplitByTrolleyJustification(
            @Schema(description = "The id of the order.") String order,
            @Schema(description = "The number of trolleys that pick items of the order.") long trolleyCount)
            implements
                PickingPlanJustification {

        @Override
        public String getDescription() {
            return "Order '%s' is prepared on %d trolley(s).".formatted(order, trolleyCount);
        }
    }
}
