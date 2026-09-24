package org.acme.conferencescheduling.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The conference scheduling planning problem input.")
public record ConferenceScheduleInput(
        @Schema(description = "Name of the conference.", required = true, minLength = 1) String name,
        @Schema(description = "Talk types restricting compatible timeslots and rooms.", required = true,
                minItems = 1) List<TalkTypeDTO> talkTypes,
        @Schema(description = "Timeslots a talk can be assigned to.", required = true,
                minItems = 1) List<TimeslotDTO> timeslots,
        @Schema(description = "Rooms a talk can be assigned to.", required = true, minItems = 1) List<RoomDTO> rooms,
        @Schema(description = "Speakers presenting the talks.", required = true, minItems = 1) List<SpeakerDTO> speakers,
        @Schema(description = "Talks that must each be assigned to a timeslot and a room.", required = true,
                minItems = 1) List<TalkDTO> talks)
        implements
            ModelInput {

    public ConferenceScheduleInput withTalks(List<TalkDTO> talks) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }
}
