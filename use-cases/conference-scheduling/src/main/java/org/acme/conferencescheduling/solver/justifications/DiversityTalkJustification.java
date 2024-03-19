package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Talk;

public record DiversityTalkJustification(String description) implements ConstraintJustification {

    public DiversityTalkJustification(String type, Talk talk, Collection<String> values, Talk talk2,
            Collection<String> values2) {
        this("Talks [%s, %s] match %s [%s] at same time.".formatted(talk.getCode(), talk2.getCode(), type,
                values.stream().filter(values2::contains).collect(joining(", "))));
    }

    public DiversityTalkJustification(String type, Talk talk, String value, Talk talk2,
            String value2) {
        this("Talks [%s, %s] have different %s [%s, %s].".formatted(talk.getCode(), talk2.getCode(), type, value, value2));
    }
}
