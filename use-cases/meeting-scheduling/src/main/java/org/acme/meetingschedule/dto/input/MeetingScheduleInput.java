package org.acme.meetingschedule.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The meeting scheduling planning problem input.")
public record MeetingScheduleInput(
        @Schema(description = "People who can attend the meetings.", required = true,
                minItems = 1) List<PersonInputDTO> people,
        @Schema(description = "Rooms the meetings can be held in.", required = true,
                minItems = 1) List<RoomInputDTO> rooms,
        @Schema(description = "The office hours the meetings have to fit in, and how finely they are divided.",
                required = true) TimeConfigurationDTO timeConfiguration,
        @Schema(description = "Meetings that must each be assigned a room and a start.", required = true,
                minItems = 1) List<MeetingInputDTO> meetings)
        implements
            ModelInput {

    public MeetingScheduleInput withMeetings(List<MeetingInputDTO> meetings) {
        return new MeetingScheduleInput(people, rooms, timeConfiguration, meetings);
    }
}
