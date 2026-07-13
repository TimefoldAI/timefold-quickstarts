package org.acme.conferencescheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The conference scheduling planning problem output.")
public record ConferenceScheduleOutput(
        @Schema(description = "Name of the conference.") String name,
        @Schema(description = "Talk types restricting compatible timeslots and rooms.") List<TalkTypeDTO> talkTypes,
        @Schema(description = "Timeslots a talk can be assigned to.") List<TimeslotDTO> timeslots,
        @Schema(description = "Rooms a talk can be assigned to.") List<RoomDTO> rooms,
        @Schema(description = "Speakers presenting the talks.") List<SpeakerDTO> speakers,
        @Schema(description = "Talks with their assigned timeslot and room.") List<TalkDTO> talks,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public ConferenceScheduleOutput {
        name = name == null ? "" : name;
        talkTypes = immutableCopy(talkTypes);
        timeslots = immutableCopy(timeslots);
        rooms = immutableCopy(rooms);
        speakers = immutableCopy(speakers);
        talks = immutableCopy(talks);
        score = score == null ? "" : score;
    }

    private static <T> List<T> immutableCopy(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
