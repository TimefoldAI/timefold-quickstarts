package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record ProhibitedTagsJustification(String description) implements ConstraintJustification {

    public ProhibitedTagsJustification(String type, Talk talk, Collection<String> prohibitedTags,
            Collection<String> actualTags) {
        this("Talk %s has prohibited %s tags [%s]".formatted(talk.getCode(), type,
                prohibitedTags.stream().filter(actualTags::contains).collect(joining(", "))));
    }

    public ProhibitedTagsJustification(String type, Collection<Speaker> speakers, Collection<String> prohibitedTags,
            Collection<String> actualTags) {
        this("Speakers [%s] have prohibited %s tags [%s]".formatted(speakers.stream().map(Speaker::getName).collect(joining(", ")),
                type, prohibitedTags.stream().filter(actualTags::contains).collect(joining(", "))));
    }
}
