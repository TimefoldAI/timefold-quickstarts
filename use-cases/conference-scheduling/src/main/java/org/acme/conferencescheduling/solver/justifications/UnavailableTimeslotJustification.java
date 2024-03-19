package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.Timeslot;

public record UnavailableTimeslotJustification(String description) implements ConstraintJustification {

    public UnavailableTimeslotJustification(Talk talk) {
        this("The timeslot %s of Talk %s has been marked as unavailable for room %s [%s].".formatted(talk.getTimeslot().getId(),
                talk.getCode(), talk.getRoom().getId(),
                talk.getRoom().getUnavailableTimeslots().stream()
                        .map(Timeslot::getId)
                        .collect(joining(", "))));
    }

    public UnavailableTimeslotJustification(Talk talk, Speaker speaker) {
        this("The timeslot %s of Talk %s has been marked as unavailable for speaker %s [%s].".formatted(
                talk.getTimeslot().getId(), talk.getCode(), speaker.getId(),
                speaker.getUnavailableTimeslots().stream()
                        .map(Timeslot::getId)
                        .collect(joining(", "))));
    }
}
