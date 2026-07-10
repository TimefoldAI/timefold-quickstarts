package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record ConflictTalkJustification(String description) implements ConstraintJustification {

    public ConflictTalkJustification(String type, Talk talk1, Collection<String> values1, Talk talk2,
            Collection<String> values2) {
        this("Two talks [%s, %s] of same %s [%s] at same time.".formatted(talk1.getCode(), talk2.getCode(), type,
                values1.stream()
                        .filter(values2::contains)
                        .collect(joining(", "))));
    }

    public ConflictTalkJustification(String type, String type2, Talk talk1, Collection<String> values1,
            Collection<String> values2, Talk talk2,
            Collection<String> values3, Collection<String> values4) {
        this("Two talks [%s, %s] on the same %s [%s] and with the same %s [%s] at the same time.".formatted(
                talk1.getCode(), talk2.getCode(), type,
                values1.stream()
                        .filter(values3::contains)
                        .collect(joining(", ")),
                type2,
                values2.stream()
                        .filter(values4::contains)
                        .collect(joining(", "))));
    }

    public ConflictTalkJustification(Talk talk1, Talk talk2, Speaker speaker) {
        this("Speaker %s has been assigned to give two talks [%s, %s] at same time.".formatted(speaker.getName(),
                talk1.getCode(), talk2.getCode()));
    }
}
