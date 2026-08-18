package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The room of a talk carries a room tag prohibited by its speakers.")
public record ProhibitedRoomTagsForSpeakersJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The ids of the speakers presenting the talk.") List<String> speakers,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The prohibited room tags the room carries.") List<String> prohibitedTags,
        @Schema(description = "The room tags the room actually carries.") List<String> roomTags)
        implements
            ConferenceSchedulingJustification {

    public static ProhibitedRoomTagsForSpeakersJustification of(Talk talk) {
        List<String> prohibitedTags = JustificationHelper.speakerTags(talk.getSpeakers(), Speaker::prohibitedRoomTags);
        return new ProhibitedRoomTagsForSpeakersJustification(talk.getCode(),
                JustificationHelper.speakerIds(talk.getSpeakers()),
                talk.getRoom().id(), JustificationHelper.shared(prohibitedTags, talk.getRoom().tags()),
                List.copyOf(talk.getRoom().tags()));
    }

    @Override
    public String getDescription() {
        return "Room '%s' of talk '%s' carries the room tags [%s] prohibited by speakers [%s]."
                .formatted(room, talk, String.join(", ", prohibitedTags), String.join(", ", speakers));
    }
}
