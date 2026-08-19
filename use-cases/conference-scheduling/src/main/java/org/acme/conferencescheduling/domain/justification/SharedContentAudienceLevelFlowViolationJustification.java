package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks share content, but the talk with the higher audience level is not scheduled after the talk "
        + "with the lower audience level, breaking the rising audience level flow.")
public record SharedContentAudienceLevelFlowViolationJustification(
        @Schema(description = "The code of the talk with the lower audience level.") String talk,
        @Schema(description = "The audience level of that talk.") int audienceLevel,
        @Schema(description = "The id of the timeslot that talk is assigned to.") String timeslot,
        @Schema(description = "The code of the talk with the higher audience level.") String higherLevelTalk,
        @Schema(description = "The audience level of that talk.") int higherAudienceLevel,
        @Schema(description = "The id of the timeslot that talk is assigned to.") String higherLevelTimeslot,
        @Schema(description = "The content tags both talks have in common.") List<String> sharedContentTags)
        implements
            ConferenceSchedulingJustification {

    public static SharedContentAudienceLevelFlowViolationJustification of(Talk talk, Talk higherLevelTalk) {
        return new SharedContentAudienceLevelFlowViolationJustification(talk.getCode(), talk.getAudienceLevel(),
                talk.getTimeslot().getId(), higherLevelTalk.getCode(), higherLevelTalk.getAudienceLevel(),
                higherLevelTalk.getTimeslot().getId(),
                JustificationHelper.shared(talk.getContentTags(), higherLevelTalk.getContentTags()));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' (audience level %d, timeslot '%s') and '%s' (audience level %d, timeslot '%s') share the content tags [%s], but the higher audience level talk is not scheduled after the lower one."
                .formatted(talk, audienceLevel, timeslot, higherLevelTalk, higherAudienceLevel, higherLevelTimeslot,
                        String.join(", ", sharedContentTags));
    }
}
