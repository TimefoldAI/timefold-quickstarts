package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The room of a talk does not carry every room tag the talk itself prefers.")
public record MissingPreferredRoomTagsForTalkJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The preferred room tags the room does not carry.") List<String> missingTags,
        @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
        implements
            ConferenceSchedulingJustification {

    public static MissingPreferredRoomTagsForTalkJustification of(Talk talk) {
        return new MissingPreferredRoomTagsForTalkJustification(talk.getCode(), talk.getRoom().id(),
                JustificationHelper.missing(talk.getPreferredRoomTags(), talk.getRoom().tags()),
                List.copyOf(talk.getRoom().tags()));
    }

    @Override
    public String getDescription() {
        return "Room '%s' of talk '%s' is missing the preferred room tags [%s]."
                .formatted(room, talk, String.join(", ", missingTags));
    }
}
