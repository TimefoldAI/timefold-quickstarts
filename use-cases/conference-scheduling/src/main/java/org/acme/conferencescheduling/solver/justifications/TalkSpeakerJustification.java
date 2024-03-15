package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record TalkSpeakerJustification(String description, Talk talk, Speaker speaker) implements ConstraintJustification {

    public TalkSpeakerJustification(String description, Talk talk, Speaker speaker) {
        this.talk = talk;
        this.speaker = speaker;
        this.description = "%s talk %s and speaker %s".formatted(description, talk.getTitle(), speaker.getName());
    }
}
