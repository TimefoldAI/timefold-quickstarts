package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record UnavailableTimeslotJustification(String description) implements ConstraintJustification {

    public UnavailableTimeslotJustification(Talk talk) {
        this("Unavailable timeslot for %s".formatted(talk.getTitle()));
    }

    public UnavailableTimeslotJustification(Talk talk, Speaker speaker) {
        this("Unavailable timeslot for talk %s and speaker %s".formatted(talk.getTitle(), speaker.getName()));
    }
}
