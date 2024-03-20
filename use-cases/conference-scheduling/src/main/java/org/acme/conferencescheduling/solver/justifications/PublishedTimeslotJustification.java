package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Talk;

public record PublishedTimeslotJustification(String description) implements ConstraintJustification {

    public PublishedTimeslotJustification(Talk talk, boolean isTimeslot) {
        this("The %s %s of Talk %s is different from the published one %s.".formatted(isTimeslot ? "timeslot" : "room",
                isTimeslot ? talk.getTimeslot().getId() : talk.getRoom().getId(), talk.getCode(),
                isTimeslot ? talk.getPublishedTimeslot().getId() : talk.getPublishedRoom().getId()));
    }
}
