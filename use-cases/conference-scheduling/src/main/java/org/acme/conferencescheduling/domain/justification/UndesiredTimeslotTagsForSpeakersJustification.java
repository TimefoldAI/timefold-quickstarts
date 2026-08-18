package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk carries a timeslot tag its speakers find undesired.")
public record UndesiredTimeslotTagsForSpeakersJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The undesired timeslot tags the timeslot carries.") List<String> undesiredTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static UndesiredTimeslotTagsForSpeakersJustification of(Talk talk) {
        List<String> undesiredTags = JustificationHelper.speakerTags(talk.getSpeakers(), Speaker::undesiredTimeslotTags);
        return new UndesiredTimeslotTagsForSpeakersJustification(talk.getCode(),
                JustificationHelper.speakerIds(talk.getSpeakers()),
                talk.getTimeslot().getId(), JustificationHelper.shared(undesiredTags, talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' carries the timeslot tags [%s] undesired by speakers [%s]."
                .formatted(timeslot, talk, String.join(", ", undesiredTags), String.join(", ", speakers));
    }
}
