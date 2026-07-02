package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record ConferenceSchedulingJustification(String description) implements ConstraintJustification {

}
