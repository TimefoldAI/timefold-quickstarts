package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record ProhibitedTagsJustification(String description) implements ConstraintJustification {

    public ProhibitedTagsJustification(Talk talk, Collection<String> expectedTags, Collection<String> actualTags) {
        this("Prohibited tags for talk %s. Prohibited [%s]; Actual[%s]".formatted(talk.getTitle(),
                String.join(", ", expectedTags),
                String.join(", ", actualTags)));
    }

    public ProhibitedTagsJustification(Collection<Speaker> speakers, Collection<String> expectedTags,
            Collection<String> actualTags) {
        this("Prohibited tags for speakers: %s. Prohibited [%s]; Actual[%s]".formatted(
                speakers.stream().map(Speaker::getName).collect(joining(", ")), String.join(", ", expectedTags),
                String.join(", ", actualTags)));
    }
}
