package org.acme.conferencescheduling.domain.justification;

import java.time.LocalDate;
import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Two talks sharing a theme track on the same day are scheduled in different rooms.")
public record TalksWithSameThemeTrackInDifferentRoomsJustification(
        @Schema(description = "The day on which both talks are scheduled.") LocalDate date,
        @Schema(description = "The code of the first talk.") String talk,
        @Schema(description = "The id of the room the first talk is assigned to.") String room,
        @Schema(description = "The code of the second talk.") String otherTalk,
        @Schema(description = "The id of the room the second talk is assigned to.") String otherRoom,
        @Schema(description = "The theme track tags both talks have in common.") List<String> sharedThemeTrackTags)
        implements
            ConferenceSchedulingJustification {

    public static TalksWithSameThemeTrackInDifferentRoomsJustification of(Talk talk, Talk otherTalk) {
        return new TalksWithSameThemeTrackInDifferentRoomsJustification(
                talk.getTimeslot().getStartDateTime().toLocalDate(), talk.getCode(), talk.getRoom().id(),
                otherTalk.getCode(), otherTalk.getRoom().id(),
                JustificationHelper.shared(talk.getThemeTrackTags(), otherTalk.getThemeTrackTags()));
    }

    @Override
    public String getDescription() {
        return "Talks '%s' and '%s' on %s share the theme track tags [%s] but are scheduled in different rooms '%s' and '%s'."
                .formatted(talk, otherTalk, date, String.join(", ", sharedThemeTrackTags), room, otherRoom);
    }
}
