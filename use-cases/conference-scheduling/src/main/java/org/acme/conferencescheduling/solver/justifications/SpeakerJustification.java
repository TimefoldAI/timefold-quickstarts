package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record SpeakerJustification(String description, Speaker speaker) implements ConstraintJustification {

    public SpeakerJustification(String description, Speaker speaker) {
        this.speaker = speaker;
        this.description = "%s %s".formatted(description, speaker.getName());
    }
}
