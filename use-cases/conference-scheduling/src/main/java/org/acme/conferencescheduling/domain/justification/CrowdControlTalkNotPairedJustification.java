package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk that needs crowd control is not paired with exactly one other crowd-control talk "
        + "in the same timeslot.")
public record CrowdControlTalkNotPairedJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The crowd control risk of the talk.") int crowdControlRisk,
        @Schema(description = "The number of other crowd-control talks in the same timeslot; exactly one is required.") long pairedTalkCount)
        implements
            ConferenceSchedulingJustification {

    public static CrowdControlTalkNotPairedJustification of(Talk talk, long pairedTalkCount) {
        return new CrowdControlTalkNotPairedJustification(talk.getCode(), talk.getTimeslot().getId(),
                talk.getCrowdControlRisk(), pairedTalkCount);
    }

    @Override
    public String getDescription() {
        return "Talk '%s' with crowd control risk %d is paired with %d other crowd-control talks in timeslot '%s', but exactly 1 is required."
                .formatted(talk, crowdControlRisk, pairedTalkCount, timeslot);
    }
}
