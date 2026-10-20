package org.acme.orderpicking.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class PickingPlanConstraintGroup {

    public static final ConstraintGroupInfo BUCKET_CAPACITY = new ConstraintGroupInfo("bucketCapacity",
            "Bucket capacity",
            "Give every trolley enough buckets for the orders it picks, without sharing a bucket between two orders.",
            "IconBucket",
            new String[] { "bucket capacity" });

    public static final ConstraintGroupInfo TRAVEL_DISTANCE = new ConstraintGroupInfo("travelDistance",
            "Travel distance",
            "Keep the path each trolley walks through the warehouse, from its start location and back, as short as possible.",
            "IconRoute",
            new String[] { "travel distance" });

    public static final ConstraintGroupInfo ORDER_COHESION = new ConstraintGroupInfo("orderCohesion",
            "Order cohesion",
            "Prepare each order on as few trolleys as possible.",
            "IconPackages",
            new String[] { "order cohesion" });

    private PickingPlanConstraintGroup() {
    }
}
