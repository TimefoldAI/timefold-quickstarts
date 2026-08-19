package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks in the same timeslot are given in the same language.")
public record TalksWithSameLanguageInSameTimeslotJustification(
        @Schema(description = "The id of the timeslot both talks are assigned to.") String timeslot,
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The language both talks are given in.") String language)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithSameLanguageInSameTimeslotJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithSameLanguageInSameTimeslotJustification(talk.getTimeslot().getId(), talk.getCode(),
                otherTalk.getCode(), talk.getLanguage());
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' in timeslot '%s' are both given in language '%s'."
                .formatted(talk, otherTalk, timeslot, language);
    }
}
