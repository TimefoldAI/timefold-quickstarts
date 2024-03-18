package org.acme.conferencescheduling.solver.justifications;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record ConflictTalkJustification(String description) implements ConstraintJustification {

    public ConflictTalkJustification(Talk talk1, Talk talk2) {
        this("Conflict between talks %s(%s - %s) and %s(%s - %s)".formatted(talk1.getTitle(),
                talk1.getTimeslot().getStartDateTime(),
                talk1.getTimeslot().getEndDateTime(), talk2.getTitle(), talk2.getTimeslot().getStartDateTime(),
                talk2.getTimeslot().getEndDateTime()));
    }

    public ConflictTalkJustification(Talk talk1, Collection<String> tags, Talk talk2, Collection<String> tags2) {
        this("Conflict between talks %s [%s] and %s [%s]".formatted(talk1.getTitle(), String.join(", ", tags), talk2.getTitle(),
                String.join(", ", tags2)));
    }

    public ConflictTalkJustification(Talk talk1, Talk talk2, Speaker speaker) {
        this("Conflict between talks %s(%s - %s) and %s(%s - %s) for speaker %s".formatted(talk1.getTitle(),
                talk1.getTimeslot().getStartDateTime(),
                talk1.getTimeslot().getEndDateTime(), talk2.getTitle(), talk2.getTimeslot().getStartDateTime(),
                talk2.getTimeslot().getEndDateTime(), speaker.getName()));
    }
}
