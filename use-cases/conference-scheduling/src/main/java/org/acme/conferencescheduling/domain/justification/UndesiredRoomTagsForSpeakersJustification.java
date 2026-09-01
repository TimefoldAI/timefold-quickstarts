package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The room of a talk carries a room tag its speakers find undesired.")
public record UndesiredRoomTagsForSpeakersJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The undesired room tags the room carries.") List<String> undesiredTags,
        @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
        implements
            ConferenceSchedulingJustification {

    public static UndesiredRoomTagsForSpeakersJustification of(Talk talk) {
        List<String> undesiredTags = JustificationHelper.speakerTags(talk.getSpeakers(), Speaker::undesiredRoomTags);
        return new UndesiredRoomTagsForSpeakersJustification(talk.getCode(), JustificationHelper.speakerIds(talk.getSpeakers()),
                talk.getRoom().id(), JustificationHelper.shared(undesiredTags, talk.getRoom().tags()),
                List.copyOf(talk.getRoom().tags()));
    }

    @Override
    public String getDescription() {
        return "Room '%s' of talk '%s' carries the room tags [%s] undesired by speakers [%s]."
                .formatted(room, talk, String.join(", ", undesiredTags), String.join(", ", speakers));
    }
}
