package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk carries a timeslot tag the talk itself finds undesired.")
public record UndesiredTimeslotTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The undesired timeslot tags the timeslot carries.") List<String> undesiredTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static UndesiredTimeslotTagsForTalkJustification of(Talk talk) {
        return new UndesiredTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                JustificationHelper.shared(talk.getUndesiredTimeslotTags(), talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' carries the undesired timeslot tags [%s]."
                .formatted(timeslot, talk, String.join(", ", undesiredTags));
    }
}
