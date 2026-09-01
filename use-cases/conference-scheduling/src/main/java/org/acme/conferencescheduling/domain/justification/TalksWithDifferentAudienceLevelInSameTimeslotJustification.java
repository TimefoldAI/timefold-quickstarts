package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks in the same timeslot target a different audience level, which is rewarded.")
public record TalksWithDifferentAudienceLevelInSameTimeslotJustification(
        @Schema(description = "The id of the timeslot both talks are assigned to.") String timeslot,
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The audience level of the first talk.") int audienceLevel,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The audience level of the second talk.") int otherAudienceLevel)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithDifferentAudienceLevelInSameTimeslotJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithDifferentAudienceLevelInSameTimeslotJustification(talk.getTimeslot().getId(), talk.getCode(),
                talk.getAudienceLevel(), otherTalk.getCode(), otherTalk.getAudienceLevel());
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' in timeslot '%s' have the different audience levels %d and %d."
                .formatted(talk, otherTalk, timeslot, audienceLevel, otherAudienceLevel);
    }
}
