package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk does not carry every timeslot tag the talk itself requires.")
public record MissingRequiredTimeslotTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The required timeslot tags the timeslot does not carry.") List<String> missingTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static MissingRequiredTimeslotTagsForTalkJustification of(Talk talk) {
        return new MissingRequiredTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                JustificationHelper.missing(talk.getRequiredTimeslotTags(), talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' is missing the required timeslot tags [%s]."
                .formatted(timeslot, talk, String.join(", ", missingTags));
    }
}
