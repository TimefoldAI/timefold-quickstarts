package org.acme.conferencescheduling.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.acme.conferencescheduling.dto.input.RoomDTO;
import org.acme.conferencescheduling.dto.input.SpeakerDTO;
import org.acme.conferencescheduling.dto.input.TalkDTO;
import org.acme.conferencescheduling.dto.input.TalkTypeDTO;
import org.acme.conferencescheduling.dto.input.TimeslotDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The conference scheduling planning problem output.")
public record ConferenceScheduleOutput(
        @Schema(description = "Name of the conference.", required = true) String name,
        @Schema(description = "Talk types restricting compatible timeslots and rooms.",
                required = true) List<TalkTypeDTO> talkTypes,
        @Schema(description = "Timeslots a talk can be assigned to.", required = true) List<TimeslotDTO> timeslots,
        @Schema(description = "Rooms a talk can be assigned to.", required = true) List<RoomDTO> rooms,
        @Schema(description = "Speakers presenting the talks.", required = true) List<SpeakerDTO> speakers,
        @Schema(description = "Talks with their assigned timeslot and room.", required = true) List<TalkDTO> talks,
        @Schema(description = "The score of the solution.", required = true) String score) implements ModelOutput {
}
