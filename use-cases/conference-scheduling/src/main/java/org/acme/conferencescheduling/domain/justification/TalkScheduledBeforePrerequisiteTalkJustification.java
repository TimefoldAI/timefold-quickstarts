package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk starts before one of its prerequisite talks has finished.")
public record TalkScheduledBeforePrerequisiteTalkJustification(
        @Schema(description = "The code of the talk that depends on the prerequisite.") String talk,
        @Schema(description = "The id of the timeslot the dependent talk is assigned to.") String timeslot,
        @Schema(description = "The code of the prerequisite talk.") String prerequisiteTalk,
        @Schema(description = "The id of the timeslot the prerequisite talk is assigned to.") String prerequisiteTimeslot)
        implements
            ConferenceSchedulingJustification {

    public static TalkScheduledBeforePrerequisiteTalkJustification of(Talk prerequisiteTalk, Talk talk) {
        return new TalkScheduledBeforePrerequisiteTalkJustification(talk.getCode(), talk.getTimeslot().getId(),
                prerequisiteTalk.getCode(), prerequisiteTalk.getTimeslot().getId());
    }

    @Override
    public String getDescription() {
        return "Talk '%s' in timeslot '%s' must be scheduled after its prerequisite talk '%s' in timeslot '%s'."
                .formatted(talk, timeslot, prerequisiteTalk, prerequisiteTimeslot);
    }
}
