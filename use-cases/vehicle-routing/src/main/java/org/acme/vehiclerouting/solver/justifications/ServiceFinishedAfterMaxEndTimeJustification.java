package org.acme.vehiclerouting.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record ServiceFinishedAfterMaxEndTimeJustification(String customerId, long serviceFinishedDelayInMinutes,
        String description) implements ConstraintJustification {

    public ServiceFinishedAfterMaxEndTimeJustification(String customerId, long serviceFinishedDelayInMinutes) {
        this(customerId, serviceFinishedDelayInMinutes, "Customer '%s' serviced with a %s-minute delay."
                .formatted(customerId, serviceFinishedDelayInMinutes));
    }
}
