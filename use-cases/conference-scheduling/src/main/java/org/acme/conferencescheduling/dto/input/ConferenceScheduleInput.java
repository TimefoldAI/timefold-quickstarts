package org.acme.conferencescheduling.dto.input;

import java.util.List;

import jakarta.validation.Valid;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@Schema(description = "The conference scheduling planning problem input.")
public record ConferenceScheduleInput(
        @Schema(description = "Name of the conference.", required = true) String name,
        @Schema(description = "Talk types restricting compatible timeslots and rooms.",
                required = true) @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid TalkTypeDTO> talkTypes,
        @Schema(description = "Timeslots a talk can be assigned to.",
                required = true) @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid TimeslotDTO> timeslots,
        @Schema(description = "Rooms a talk can be assigned to.",
                required = true) @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid RoomDTO> rooms,
        @Schema(description = "Speakers presenting the talks.",
                required = true) @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid SpeakerDTO> speakers,
        @Schema(description = "Talks that must each be assigned to a timeslot and a room.",
                required = true) @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid TalkDTO> talks)
        implements
            ModelInput {

    public ConferenceScheduleInput withTalks(List<TalkDTO> talks) {
        return new ConferenceScheduleInput(name, talkTypes, timeslots, rooms, speakers, talks);
    }
}
