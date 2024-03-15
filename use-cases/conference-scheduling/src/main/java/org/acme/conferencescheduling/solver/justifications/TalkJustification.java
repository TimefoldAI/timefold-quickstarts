package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Talk;

public record TalkJustification(String description, Talk talk) implements ConstraintJustification {

    public TalkJustification(String description, Talk talk) {
        this.talk = talk;
        this.description = "%s %s".formatted(description, talk.getTitle());
    }
}
