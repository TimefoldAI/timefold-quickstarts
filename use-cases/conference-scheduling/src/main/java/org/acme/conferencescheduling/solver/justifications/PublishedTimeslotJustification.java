package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Talk;

public record PublishedTimeslotJustification(String description) implements ConstraintJustification {

    public PublishedTimeslotJustification(Talk talk, boolean isTimeslot) {
        this("Published %s for talk %s".formatted(isTimeslot ? "timeslot" : "room", talk.getTitle()));
    }
}
