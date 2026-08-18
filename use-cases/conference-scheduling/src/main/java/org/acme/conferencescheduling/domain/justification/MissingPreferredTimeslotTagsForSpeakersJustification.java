package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk does not carry every timeslot tag preferred by its speakers.")
public record MissingPreferredTimeslotTagsForSpeakersJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The preferred timeslot tags the timeslot does not carry.") List<String> missingTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static MissingPreferredTimeslotTagsForSpeakersJustification of(Talk talk) {
        List<String> preferredTags = JustificationHelper.speakerTags(talk.getSpeakers(), Speaker::preferredTimeslotTags);
        return new MissingPreferredTimeslotTagsForSpeakersJustification(talk.getCode(), JustificationHelper.speakerIds(talk.getSpeakers()),
                talk.getTimeslot().getId(), JustificationHelper.missing(preferredTags, talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' is missing the timeslot tags [%s] preferred by speakers [%s]."
                .formatted(timeslot, talk, String.join(", ", missingTags), String.join(", ", speakers));
    }
}
