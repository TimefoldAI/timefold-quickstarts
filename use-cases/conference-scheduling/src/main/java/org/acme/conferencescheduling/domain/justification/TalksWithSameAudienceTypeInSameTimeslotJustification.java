package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks in the same timeslot share an audience type, which is rewarded.")
public record TalksWithSameAudienceTypeInSameTimeslotJustification(
        @Schema(description = "The id of the timeslot both talks are assigned to.") String timeslot,
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The audience types both talks have in common.") List<String> sharedAudienceTypes)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithSameAudienceTypeInSameTimeslotJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithSameAudienceTypeInSameTimeslotJustification(talk.getTimeslot().getId(), talk.getCode(),
                otherTalk.getCode(), JustificationHelper.shared(talk.getAudienceTypes(), otherTalk.getAudienceTypes()));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' in timeslot '%s' share the audience types [%s]."
                .formatted(talk, otherTalk, timeslot, String.join(", ", sharedAudienceTypes));
    }
}
