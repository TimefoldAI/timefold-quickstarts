package org.acme.vehiclerouting.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class VehicleRoutePlanConstraintGroup {

    public static final ConstraintGroupInfo VEHICLE_CAPACITY = new ConstraintGroupInfo("vehicleCapacity",
            "Vehicle capacity",
            "Keep the total demand of the visits on a vehicle's route within the capacity that vehicle has.",
            "IconTruckLoading",
            new String[] { "vehicle capacity" });

    public static final ConstraintGroupInfo TIME_WINDOWS = new ConstraintGroupInfo("timeWindows",
            "Time windows",
            "Service every visit inside the time window it accepts a vehicle in.",
            "IconClock",
            new String[] { "time windows" });

    public static final ConstraintGroupInfo VISIT_ASSIGNMENT = new ConstraintGroupInfo("visitAssignment",
            "Visit assignment",
            "Get as many visits as possible onto a vehicle's route, rather than leaving them unserviced.",
            "IconMapPin",
            new String[] { "visit assignment" });

    public static final ConstraintGroupInfo TRAVEL_TIME = new ConstraintGroupInfo("travelTime",
            "Travel time",
            "Keep the fleet on the road for as little time as possible.",
            "IconRoute",
            new String[] { "travel time" });

    private VehicleRoutePlanConstraintGroup() {
    }
}
