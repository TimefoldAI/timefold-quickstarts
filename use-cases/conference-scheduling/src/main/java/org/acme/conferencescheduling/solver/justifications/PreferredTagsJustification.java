package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record PreferredTagsJustification(String description) implements ConstraintJustification {

    public PreferredTagsJustification(String type, Talk talk, Collection<String> expectedTags, Collection<String> actualTags) {
        this("Missing preferred %s tags [%s] for talk %s."
                .formatted(type,
                        expectedTags.stream().filter(t -> !actualTags.contains(t)).collect(joining(", ")),
                        talk.getCode()));
    }

    public PreferredTagsJustification(String type, Collection<Speaker> speakers, Collection<String> expectedTags,
                                     Collection<String> actualTags) {
        this("Missing preferred %s tags [%s] for speakers [%s].".formatted(type,
                expectedTags.stream().filter(t -> !actualTags.contains(t)).collect(joining(", ")),
                speakers.stream().map(Speaker::getName).collect(joining(", "))));
    }
}
