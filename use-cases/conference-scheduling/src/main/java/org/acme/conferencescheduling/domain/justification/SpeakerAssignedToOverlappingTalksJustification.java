package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A speaker is assigned to two talks whose timeslots overlap.")
public record SpeakerAssignedToOverlappingTalksJustification(
        @Schema(description = "The id of the double-booked speaker.") String speaker,
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
        implements
            ConferenceSchedulingJustification {

    public static SpeakerAssignedToOverlappingTalksJustification of(Talk talk, Talk otherTalk, Speaker speaker) {
        return new SpeakerAssignedToOverlappingTalksJustification(speaker.id(), talk.getCode(), otherTalk.getCode(),
                otherTalk.overlappingDurationInMinutes(talk));
    }

    @Override
    public String getDescription() {
        return "Speaker '%s' is assigned to talks '%s' and '%s', which overlap for %d minutes."
                .formatted(speaker, talk, otherTalk, overlapInMinutes);
    }
}
