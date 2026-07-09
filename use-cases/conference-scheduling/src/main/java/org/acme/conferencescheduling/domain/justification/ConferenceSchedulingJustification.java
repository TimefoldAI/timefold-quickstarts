package org.acme.conferencescheduling.domain.justification;

import java.util.Objects;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record ConferenceSchedulingJustification(String description) implements ConstraintJustification {

    public ConferenceSchedulingJustification {
        Objects.requireNonNull(description);
    }

}
