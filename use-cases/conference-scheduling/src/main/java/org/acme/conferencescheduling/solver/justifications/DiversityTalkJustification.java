package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Talk;

public record DiversityTalkJustification(String description) implements ConstraintJustification {

    public DiversityTalkJustification(Talk talk, Talk talk2) {
        this("Diversity between talks %s and %s".formatted(talk.getTitle(), talk2.getTitle()));
    }
}
