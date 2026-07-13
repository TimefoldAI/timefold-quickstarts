package org.acme.conferencescheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The conference scheduling planning problem input.")
public record ConferenceScheduleInput(
        @Schema(description = "Name of the conference.") String name,
        @Schema(description = "Talk types restricting compatible timeslots and rooms.") List<TalkTypeDTO> talkTypes,
        @Schema(description = "Timeslots a talk can be assigned to.") List<TimeslotDTO> timeslots,
        @Schema(description = "Rooms a talk can be assigned to.") List<RoomDTO> rooms,
        @Schema(description = "Speakers presenting the talks.") List<SpeakerDTO> speakers,
        @Schema(description = "Talks that must each be assigned to a timeslot and a room.") List<TalkDTO> talks)
        implements
            ModelInput {

    public ConferenceScheduleInput {
        name = name == null ? "" : name;
        talkTypes = immutableCopy(talkTypes);
        timeslots = immutableCopy(timeslots);
        rooms = immutableCopy(rooms);
        speakers = immutableCopy(speakers);
        talks = immutableCopy(talks);
    }

    private static <T> List<T> immutableCopy(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }

    public ConferenceScheduleInput withTalks(List<TalkDTO> talks) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }
}
