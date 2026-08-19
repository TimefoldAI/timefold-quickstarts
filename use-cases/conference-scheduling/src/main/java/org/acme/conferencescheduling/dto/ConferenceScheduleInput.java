package org.acme.conferencescheduling.dto;

import java.util.Collections;
import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.acme.conferencescheduling.support.ObjectHelper;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static org.acme.conferencescheduling.support.ObjectHelper.immutableCopy;

@Schema(description = "The conference scheduling planning problem input.")
public record ConferenceScheduleInput(
        @Schema(description = "Name of the conference.", required = true) String name,
        @Schema(description = "Talk types restricting compatible timeslots and rooms.",
                required = true) List<TalkTypeDTO> talkTypes,
        @Schema(description = "Timeslots a talk can be assigned to.", required = true) List<TimeslotDTO> timeslots,
        @Schema(description = "Rooms a talk can be assigned to.", required = true) List<RoomDTO> rooms,
        @Schema(description = "Speakers presenting the talks.", required = true) List<SpeakerDTO> speakers,
        @Schema(description = "Talks that must each be assigned to a timeslot and a room.",
                required = true) List<TalkDTO> talks)
        implements
            ModelInput {

    public ConferenceScheduleInput {
        talkTypes = immutableCopy(talkTypes);
        timeslots = immutableCopy(timeslots);
        rooms = immutableCopy(rooms);
        speakers = immutableCopy(speakers);
        talks = immutableCopy(talks);
    }

    public ConferenceScheduleInput withTalks(List<TalkDTO> talks) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }
}
