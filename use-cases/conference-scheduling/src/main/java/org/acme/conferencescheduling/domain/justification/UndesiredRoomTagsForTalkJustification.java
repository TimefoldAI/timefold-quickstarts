package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The room of a talk carries a room tag the talk itself finds undesired.")
public record UndesiredRoomTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The undesired room tags the room carries.") List<String> undesiredTags,
        @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
        implements
            ConferenceSchedulingJustification {

    public static UndesiredRoomTagsForTalkJustification of(Talk talk) {
        return new UndesiredRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                JustificationHelper.shared(talk.getUndesiredRoomTags(), talk.getRoom().tags()),
                List.copyOf(talk.getRoom().tags()));
    }

    @Override
    public String getDescription() {
        return "Room '%s' of talk '%s' carries the undesired room tags [%s]."
                .formatted(room, talk, String.join(", ", undesiredTags));
    }
}
