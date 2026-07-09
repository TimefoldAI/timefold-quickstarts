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
        talkTypes = talkTypes == null ? List.of() : List.copyOf(talkTypes);
        timeslots = timeslots == null ? List.of() : List.copyOf(timeslots);
        rooms = rooms == null ? List.of() : List.copyOf(rooms);
        speakers = speakers == null ? List.of() : List.copyOf(speakers);
        talks = talks == null ? List.of() : List.copyOf(talks);
        score = score == null ? "" : score;
    }

    public ConferenceScheduleOutput withName(String name) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }

    public ConferenceScheduleOutput withTalkTypes(List<TalkTypeDTO> talkTypes) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }

    public ConferenceScheduleOutput withTimeslots(List<TimeslotDTO> timeslots) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }

    public ConferenceScheduleOutput withRooms(List<RoomDTO> rooms) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }

    public ConferenceScheduleOutput withSpeakers(List<SpeakerDTO> speakers) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }

    public ConferenceScheduleOutput withTalks(List<TalkDTO> talks) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }

    public ConferenceScheduleOutput withScore(String score) {
        return new ConferenceScheduleOutput(name, talkTypes, timeslots, rooms, speakers, talks, score);
    }
}
