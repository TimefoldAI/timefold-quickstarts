package org.acme.vehiclerouting.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import java.time.Duration;

public record MinimizeTravelTimeJustification(String vehicleName, long totalDrivingTimeSeconds,
        String description) implements ConstraintJustification {

    public MinimizeTravelTimeJustification(String vehicleName, long totalDrivingTimeSeconds) {
        this(vehicleName, totalDrivingTimeSeconds, "Vehicle '%s' total travel time is %s."
                .formatted(vehicleName, formatDrivingTime(totalDrivingTimeSeconds)));
    }

    private static String formatDrivingTime(long drivingTimeSeconds) {
        Duration drivingTime = Duration.ofSeconds(drivingTimeSeconds);
        return "%s hours %s minutes".formatted(drivingTime.toHours(),
                drivingTime.toSecondsPart() >= 30 ? drivingTime.toMinutesPart() + 1 : drivingTime.toMinutesPart());
    }
}
