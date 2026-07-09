package org.acme.bedallocation.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem output.")
public record BedScheduleOutput(
        @Schema(description = "Departments that group the rooms.") List<DepartmentDTO> departments,
        @Schema(description = "Rooms available across all departments.") List<RoomDTO> rooms,
        @Schema(description = "Beds available across all rooms.") List<BedDTO> beds,
        @Schema(description = "Patient stays with their assigned bed.") List<StayDTO> stays,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public BedScheduleOutput {
        departments = List.copyOf(departments);
        rooms = List.copyOf(rooms);
        beds = List.copyOf(beds);
        stays = List.copyOf(stays);
    }

    public BedScheduleOutput withDepartments(List<DepartmentDTO> departments) {
        return new BedScheduleOutput(departments, rooms, beds, stays, score);
    }

    public BedScheduleOutput withRooms(List<RoomDTO> rooms) {
        return new BedScheduleOutput(departments, rooms, beds, stays, score);
    }

    public BedScheduleOutput withBeds(List<BedDTO> beds) {
        return new BedScheduleOutput(departments, rooms, beds, stays, score);
    }

    public BedScheduleOutput withStays(List<StayDTO> stays) {
        return new BedScheduleOutput(departments, rooms, beds, stays, score);
    }

    public BedScheduleOutput withScore(String score) {
        return new BedScheduleOutput(departments, rooms, beds, stays, score);
    }
}
