package org.acme.orderpicking.domain;

public final class PickingPlanConstraintProperties {

    public static final String REQUIRED_NUMBER_OF_BUCKETS = "Required number of buckets";

    public static final String MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK = "Minimize distance from previous pick task";
    public static final String MINIMIZE_DISTANCE_FROM_LAST_PICK_TASK_TO_PATH_ORIGIN =
            "Minimize distance from last pick task to path origin";
    public static final String MINIMIZE_ORDER_SPLIT_BY_TROLLEY = "Minimize order split by trolley";

    private PickingPlanConstraintProperties() {
    }
}
