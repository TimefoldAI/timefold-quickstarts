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
        talkTypes = talkTypes == null ? List.of() : List.copyOf(talkTypes);
        timeslots = timeslots == null ? List.of() : List.copyOf(timeslots);
        rooms = rooms == null ? List.of() : List.copyOf(rooms);
        speakers = speakers == null ? List.of() : List.copyOf(speakers);
        talks = talks == null ? List.of() : List.copyOf(talks);
    }

    public ConferenceScheduleInput withName(String name) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }

    public ConferenceScheduleInput withTalkTypes(List<TalkTypeDTO> talkTypes) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }

    public ConferenceScheduleInput withTimeslots(List<TimeslotDTO> timeslots) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }

    public ConferenceScheduleInput withRooms(List<RoomDTO> rooms) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }

    public ConferenceScheduleInput withSpeakers(List<SpeakerDTO> speakers) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }

    public ConferenceScheduleInput withTalks(List<TalkDTO> talks) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }
}
