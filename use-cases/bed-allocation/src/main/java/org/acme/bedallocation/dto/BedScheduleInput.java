package org.acme.bedallocation.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem input.")
public record BedScheduleInput(
        @Schema(description = "Departments that group the rooms.") List<DepartmentDTO> departments,
        @Schema(description = "Rooms available across all departments.") List<RoomDTO> rooms,
        @Schema(description = "Beds available across all rooms.") List<BedDTO> beds,
        @Schema(description = "Patient stays that must each be assigned to a bed.") List<StayDTO> stays)
        implements
            ModelInput {

    public BedScheduleInput {
        departments = List.copyOf(departments);
        rooms = List.copyOf(rooms);
        beds = List.copyOf(beds);
        stays = List.copyOf(stays);
    }

    public BedScheduleInput withDepartments(List<DepartmentDTO> departments) {
        return new BedScheduleInput(departments, rooms, beds, stays);
    }

    public BedScheduleInput withRooms(List<RoomDTO> rooms) {
        return new BedScheduleInput(departments, rooms, beds, stays);
    }

    public BedScheduleInput withBeds(List<BedDTO> beds) {
        return new BedScheduleInput(departments, rooms, beds, stays);
    }

    public BedScheduleInput withStays(List<StayDTO> stays) {
        return new BedScheduleInput(departments, rooms, beds, stays);
    }
}
