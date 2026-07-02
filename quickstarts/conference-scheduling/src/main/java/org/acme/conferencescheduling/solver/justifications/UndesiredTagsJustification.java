package org.acme.conferencescheduling.solver.justifications;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record UndesiredTagsJustification(String description) implements ConstraintJustification {

    public UndesiredTagsJustification(String type, Talk talk, Collection<String> undesiredTags,
                                       Collection<String> actualTags) {
        this("Talk %s has undesired %s tags [%s]".formatted(talk.getCode(), type,
                undesiredTags.stream().filter(actualTags::contains).collect(joining(", "))));
    }

    public UndesiredTagsJustification(String type, Collection<Speaker> speakers, Collection<String> undesiredTags,
                                       Collection<String> actualTags) {
        this("Speakers [%s] have undesired %s tags [%s]".formatted(speakers.stream().map(Speaker::getName).collect(joining(", ")),
                type, undesiredTags.stream().filter(actualTags::contains).collect(joining(", "))));
    }
}
