package org.acme.conferencescheduling.solver.justifications;

import java.util.Collection;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record ConferenceSchedulingJustification(String description) implements ConstraintJustification {

    public ConferenceSchedulingJustification(String prefix, Talk talk) {
        this("%s %s".formatted(prefix, talk.getTitle()));
    }

    public ConferenceSchedulingJustification(String prefix, Speaker speaker) {
        this("%s %s".formatted(prefix, speaker.getName()));
    }

    public ConferenceSchedulingJustification(String prefix, Talk talk, Talk talk2) {
        this("%s %s and %s".formatted(prefix, talk.getTitle(), talk2.getTitle()));
    }

    public ConferenceSchedulingJustification(String prefix, Talk talk, Collection<String> tags, Talk talk2,
            Collection<String> tags2) {
        this("%s %s [%s] and %s [%s]".formatted(prefix, talk.getTitle(), String.join(", ", tags), talk2.getTitle(),
                String.join(", ", tags2)));
    }
}
