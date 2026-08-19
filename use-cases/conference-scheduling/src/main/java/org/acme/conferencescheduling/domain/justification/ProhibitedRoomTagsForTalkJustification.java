package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The room of a talk carries a room tag the talk itself prohibits.")
public record ProhibitedRoomTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The prohibited room tags the room carries.") List<String> prohibitedTags,
        @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
        implements
            ConferenceSchedulingJustification {

    public static ProhibitedRoomTagsForTalkJustification of(Talk talk) {
        return new ProhibitedRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                JustificationHelper.shared(talk.getProhibitedRoomTags(), talk.getRoom().tags()),
                List.copyOf(talk.getRoom().tags()));
    }

    @Override
    public String getDescription() {
        return "Room '%s' of talk '%s' carries the prohibited room tags [%s]."
                .formatted(room, talk, String.join(", ", prohibitedTags));
    }
}
