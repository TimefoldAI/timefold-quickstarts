package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk carries a timeslot tag prohibited by its speakers.")
public record ProhibitedTimeslotTagsForSpeakersJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The prohibited timeslot tags the timeslot carries.") List<String> prohibitedTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static ProhibitedTimeslotTagsForSpeakersJustification of(Talk talk) {
        List<String> prohibitedTags = JustificationHelper.speakerTags(talk.getSpeakers(), Speaker::prohibitedTimeslotTags);
        return new ProhibitedTimeslotTagsForSpeakersJustification(talk.getCode(), JustificationHelper.speakerIds(talk.getSpeakers()),
                talk.getTimeslot().getId(), JustificationHelper.shared(prohibitedTags, talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' carries the timeslot tags [%s] prohibited by speakers [%s]."
                .formatted(timeslot, talk, String.join(", ", prohibitedTags), String.join(", ", speakers));
    }
}
