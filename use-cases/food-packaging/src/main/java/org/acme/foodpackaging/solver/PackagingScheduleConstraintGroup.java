package org.acme.foodpackaging.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class PackagingScheduleConstraintGroup {

    public static final ConstraintGroupInfo ON_TIME_DELIVERY = new ConstraintGroupInfo("onTimeDelivery",
            "On-time delivery",
            "Finish every job before the time it was promised for, and ideally before the time it was wanted.",
            "IconClock",
            new String[] { "on-time delivery" });

    public static final ConstraintGroupInfo OPERATOR_AVAILABILITY = new ConstraintGroupInfo("operatorAvailability",
            "Operator availability",
            "Keep a line operator from having to clean two of their lines at the same time.",
            "IconUsers",
            new String[] { "operator availability" });

    public static final ConstraintGroupInfo JOB_ASSIGNMENT = new ConstraintGroupInfo("jobAssignment",
            "Job assignment",
            "Produce as many of the requested jobs as the lines can fit.",
            "IconCheckCircle",
            new String[] { "job assignment" });

    public static final ConstraintGroupInfo LINE_THROUGHPUT = new ConstraintGroupInfo("lineThroughput",
            "Line throughput",
            "Spread the jobs evenly over the lines, so the whole schedule finishes as early as possible.",
            "IconGauge",
            new String[] { "line throughput" });

    private PackagingScheduleConstraintGroup() {
    }
}
