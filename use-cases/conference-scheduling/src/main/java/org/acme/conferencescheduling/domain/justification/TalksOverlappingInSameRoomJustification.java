package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks share the same room while their timeslots overlap.")
public record TalksOverlappingInSameRoomJustification(
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The id of the room both talks are assigned to.") String room,
        @Schema(description = "The number of minutes during which both talks overlap.") int overlapInMinutes)
        implements
            ConferenceSchedulingJustification {

    public static TalksOverlappingInSameRoomJustification of(Talk talk, Talk otherTalk) {
        return new TalksOverlappingInSameRoomJustification(talk.getCode(), otherTalk.getCode(), talk.getRoom().id(),
                talk.overlappingDurationInMinutes(otherTalk));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' share room '%s' and overlap for %d minutes."
                .formatted(talk, otherTalk, room, overlapInMinutes);
    }
}
