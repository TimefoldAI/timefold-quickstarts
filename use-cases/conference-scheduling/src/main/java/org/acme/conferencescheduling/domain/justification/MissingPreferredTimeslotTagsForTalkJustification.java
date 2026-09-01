package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk does not carry every timeslot tag the talk itself prefers.")
public record MissingPreferredTimeslotTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The preferred timeslot tags the timeslot does not carry.") List<String> missingTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static MissingPreferredTimeslotTagsForTalkJustification of(Talk talk) {
        return new MissingPreferredTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                JustificationHelper.missing(talk.getPreferredTimeslotTags(), talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' is missing the preferred timeslot tags [%s]."
                .formatted(timeslot, talk, String.join(", ", missingTags));
    }
}
