package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The timeslot of a talk carries a timeslot tag the talk itself prohibits.")
public record ProhibitedTimeslotTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The prohibited timeslot tags the timeslot carries.") List<String> prohibitedTags,
        @Schema(description = "The timeslot tags the timeslot actually carries.") List<String> timeslotTags)
        implements
            ConferenceSchedulingJustification {

    public static ProhibitedTimeslotTagsForTalkJustification of(Talk talk) {
        return new ProhibitedTimeslotTagsForTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                JustificationHelper.shared(talk.getProhibitedTimeslotTags(), talk.getTimeslot().getTags()),
                List.copyOf(talk.getTimeslot().getTags()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' carries the prohibited timeslot tags [%s]."
                .formatted(timeslot, talk, String.join(", ", prohibitedTags));
    }
}
