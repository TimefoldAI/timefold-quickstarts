package org.acme.conferencescheduling.domain.justification;

import java.util.List;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A talk occupies a room during a timeslot in which that room is unavailable.")
public record RoomUnavailableAtTalkTimeslotJustification(
        @Schema(description = "The talk code.") String talk,
        @Schema(description = "The id of the room the talk is assigned to.") String room,
        @Schema(description = "The id of the timeslot the talk is assigned to.") String timeslot,
        @Schema(description = "The ids of all timeslots during which the room is unavailable.") List<String> unavailableTimeslots)
        implements
            ConferenceSchedulingJustification {

    public static RoomUnavailableAtTalkTimeslotJustification of(Talk talk) {
        return new RoomUnavailableAtTalkTimeslotJustification(talk.getCode(), talk.getRoom().id(),
                talk.getTimeslot().getId(), JustificationHelper.timeslotIds(talk.getRoom().unavailableTimeslots()));
    }

    @Override
    public String getDescription() {
        return "Timeslot '%s' of talk '%s' is marked as unavailable for room '%s' [%s]."
                .formatted(timeslot, talk, room, String.join(", ", unavailableTimeslots));
    }
}
