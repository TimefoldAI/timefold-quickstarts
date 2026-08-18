package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The room of a talk does not carry every room tag preferred by its speakers.")
public record MissingPreferredRoomTagsForSpeakersJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The preferred room tags the room does not carry.") List<String> missingTags,
        @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
        implements
            ConferenceSchedulingJustification {

    public static MissingPreferredRoomTagsForSpeakersJustification of(Talk talk) {
        List<String> preferredTags = JustificationHelper.speakerTags(talk.getSpeakers(), Speaker::preferredRoomTags);
        return new MissingPreferredRoomTagsForSpeakersJustification(talk.getCode(),
                JustificationHelper.speakerIds(talk.getSpeakers()),
                talk.getRoom().id(), JustificationHelper.missing(preferredTags, talk.getRoom().tags()),
                List.copyOf(talk.getRoom().tags()));
    }

    @Override
    public String getDescription() {
        return "Room '%s' of talk '%s' is missing the room tags [%s] preferred by speakers [%s]."
                .formatted(room, talk, String.join(", ", missingTags), String.join(", ", speakers));
    }
}
