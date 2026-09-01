package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk is placed in a timeslot during which one of its speakers is unavailable.")
public record SpeakerUnavailableAtTalkTimeslotJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the unavailable speaker.") String speaker,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The ids of all timeslots during which the speaker is unavailable.") List<String> unavailableTimeslots)
        implements
            ConferenceSchedulingJustification {

    public static SpeakerUnavailableAtTalkTimeslotJustification of(Talk talk, Speaker speaker) {
        return new SpeakerUnavailableAtTalkTimeslotJustification(talk.getCode(), speaker.id(),
                talk.getTimeslot().getId(), JustificationHelper.timeslotIds(speaker.unavailableTimeslots()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' is marked as unavailable for speaker '%s' [%s]."
                .formatted(timeslot, talk, speaker, String.join(", ", unavailableTimeslots));
    }
}
