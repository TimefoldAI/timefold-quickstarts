package org.acme.orderpicking.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class OrderPickingConstraintGroup {
    public static final ConstraintGroupInfo BUCKET_CAPACITY = new ConstraintGroupInfo("bucketCapacity",
            "Bucket capacity",
            "Ensure every trolley has enough buckets to hold the order items assigned to it.",
            "IconBox",
            new String[] { ConstraintGroupTag.BUCKET_CAPACITY.getTag() });
    public static final ConstraintGroupInfo TRAVEL_EFFICIENCY = new ConstraintGroupInfo("travelEfficiency",
            "Travel efficiency",
            "Minimize the distance travelled by the trolleys while picking the order items.",
            "IconRoute",
            new String[] { ConstraintGroupTag.TRAVEL_EFFICIENCY.getTag() });
    public static final ConstraintGroupInfo ORDER_INTEGRITY = new ConstraintGroupInfo("orderIntegrity",
            "Order integrity",
            "Keep the items of a single order together on the same trolley.",
            "IconLayers",
            new String[] { ConstraintGroupTag.ORDER_INTEGRITY.getTag() });

    private OrderPickingConstraintGroup() {
    }
}
