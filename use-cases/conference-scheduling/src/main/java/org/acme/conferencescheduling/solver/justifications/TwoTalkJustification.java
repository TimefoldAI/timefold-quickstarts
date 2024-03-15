package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Talk;

public record TwoTalkJustification(String description, Talk talk1, Talk talk2) implements ConstraintJustification {

    public TwoTalkJustification(String description, Talk talk1, Talk talk2) {
        this.talk1 = talk1;
        this.talk2 = talk2;
        this.description = "%s %s and %s".formatted(description, talk1.getTitle(), talk2.getTitle());
    }
}
