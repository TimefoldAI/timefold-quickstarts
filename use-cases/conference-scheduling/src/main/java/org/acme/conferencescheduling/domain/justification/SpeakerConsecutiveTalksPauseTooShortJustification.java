package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two consecutive talks of the same speaker are not separated by the minimum required pause.")
public record SpeakerConsecutiveTalksPauseTooShortJustification(
        @Schema(description = "The ids of the speakers giving both talks.") List<String> speakers,
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The id of the timeslot the first talk is assigned to.") String timeslot,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The id of the timeslot the second talk is assigned to.") String otherTimeslot)
        implements
            ConferenceSchedulingJustification {

    public static SpeakerConsecutiveTalksPauseTooShortJustification of(Talk talk, Talk otherTalk) {
        List<Speaker> sharedSpeakers = talk.getSpeakers().stream()
                .filter(otherTalk.getSpeakers()::contains)
                .toList();
        return new SpeakerConsecutiveTalksPauseTooShortJustification(JustificationHelper.speakerIds(sharedSpeakers), talk.getCode(),
                talk.getTimeslot().getId(), otherTalk.getCode(), otherTalk.getTimeslot().getId());
    }

    @Override
    public String getDescription() {
        return "Speakers [%s] do not get the minimum pause between their consecutive talks '%s' (timeslot '%s') and '%s' (timeslot '%s')."
                .formatted(String.join(", ", speakers), talk, timeslot, otherTalk, otherTimeslot);
    }
}
