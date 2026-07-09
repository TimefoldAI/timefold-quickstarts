package org.acme.meetingschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The meeting scheduling planning problem output.")
public record MeetingScheduleOutput(
        @Schema(description = "List of people who can attend meetings.") List<PersonDTO> people,
        @Schema(description = "List of time grains a meeting can start on.") List<TimeGrainDTO> timeGrains,
        @Schema(description = "List of rooms a meeting can be assigned to.") List<RoomDTO> rooms,
        @Schema(description = "List of meetings that have been scheduled.") List<MeetingDTO> meetings,
        @Schema(description = "List of meeting assignments with their assigned time grain and room.") List<MeetingAssignmentDTO> meetingAssignments,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public MeetingScheduleOutput {
        people = List.copyOf(people);
        timeGrains = List.copyOf(timeGrains);
        rooms = List.copyOf(rooms);
        meetings = List.copyOf(meetings);
        meetingAssignments = List.copyOf(meetingAssignments);
    }

    public MeetingScheduleOutput withPeople(List<PersonDTO> people) {
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }

    public MeetingScheduleOutput withTimeGrains(List<TimeGrainDTO> timeGrains) {
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }

    public MeetingScheduleOutput withRooms(List<RoomDTO> rooms) {
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }

    public MeetingScheduleOutput withMeetings(List<MeetingDTO> meetings) {
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }

    public MeetingScheduleOutput withMeetingAssignments(List<MeetingAssignmentDTO> meetingAssignments) {
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }

    public MeetingScheduleOutput withScore(String score) {
        return new MeetingScheduleOutput(people, timeGrains, rooms, meetings, meetingAssignments, score);
    }
}
