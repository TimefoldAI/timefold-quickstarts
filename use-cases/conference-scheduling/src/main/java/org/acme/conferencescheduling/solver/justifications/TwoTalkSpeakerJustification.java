package org.acme.conferencescheduling.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;

public record TwoTalkSpeakerJustification(String description, Talk talk1, Talk talk2,
        Speaker speaker) implements ConstraintJustification {

    public TwoTalkSpeakerJustification(String description, Talk talk1, Talk talk2, Speaker speaker) {
        this.talk1 = talk1;
        this.talk2 = talk2;
        this.speaker = speaker;
        this.description = "%s talks %s and %s and speaker %s".formatted(description, talk1.getTitle(),
                talk2.getTitle(), speaker.getName());
    }
}
