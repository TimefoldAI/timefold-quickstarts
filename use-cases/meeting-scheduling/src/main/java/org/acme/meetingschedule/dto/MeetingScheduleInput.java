package org.acme.meetingschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The meeting scheduling planning problem input.")
public record MeetingScheduleInput(
        @Schema(description = "List of people who can attend meetings.") List<PersonDTO> people,
        @Schema(description = "List of time grains a meeting can start on.") List<TimeGrainDTO> timeGrains,
        @Schema(description = "List of rooms a meeting can be assigned to.") List<RoomDTO> rooms,
        @Schema(description = "List of meetings to be scheduled.") List<MeetingDTO> meetings,
        @Schema(description = "List of meeting assignments to be planned.") List<MeetingAssignmentDTO> meetingAssignments)
        implements
            ModelInput {

    public MeetingScheduleInput {
        people = List.copyOf(people);
        timeGrains = List.copyOf(timeGrains);
        rooms = List.copyOf(rooms);
        meetings = List.copyOf(meetings);
        meetingAssignments = List.copyOf(meetingAssignments);
    }

    public MeetingScheduleInput withPeople(List<PersonDTO> people) {
        return new MeetingScheduleInput(people, timeGrains, rooms, meetings, meetingAssignments);
    }

    public MeetingScheduleInput withTimeGrains(List<TimeGrainDTO> timeGrains) {
        return new MeetingScheduleInput(people, timeGrains, rooms, meetings, meetingAssignments);
    }

    public MeetingScheduleInput withRooms(List<RoomDTO> rooms) {
        return new MeetingScheduleInput(people, timeGrains, rooms, meetings, meetingAssignments);
    }

    public MeetingScheduleInput withMeetings(List<MeetingDTO> meetings) {
        return new MeetingScheduleInput(people, timeGrains, rooms, meetings, meetingAssignments);
    }

    public MeetingScheduleInput withMeetingAssignments(List<MeetingAssignmentDTO> meetingAssignments) {
        return new MeetingScheduleInput(people, timeGrains, rooms, meetings, meetingAssignments);
    }
}
