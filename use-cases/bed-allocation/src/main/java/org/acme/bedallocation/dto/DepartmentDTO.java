package org.acme.bedallocation.dto;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A hospital department, grouping rooms and specialty priorities.")
public record DepartmentDTO(
        @Schema(description = "Unique identifier of the department.", required = true) String id,
        @Schema(description = "Display name of the department.", required = true) String name,
        @Schema(description = "Minimum patient age accepted by this department, or null if there is none.") Integer minimumAge,
        @Schema(description = "Maximum patient age accepted by this department, or null if there is none.") Integer maximumAge,
        @Schema(description = "Priority (1 is highest) of each specialty treated by this department.") Map<String, Integer> specialtyToPriority,
        @Schema(description = "Rooms belonging to this department.", required = true) List<RoomDTO> rooms) {

    public DepartmentDTO {
        rooms = rooms == null ? List.of() : rooms;
    }

    public DepartmentDTO(String id, String name) {
        this(id, name, null, null, null, null);
    }

    public DepartmentDTO withMinimumAge(Integer minimumAge) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
    }

    public DepartmentDTO withMaximumAge(Integer maximumAge) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
    }

    public DepartmentDTO withSpecialtyToPriority(Map<String, Integer> specialtyToPriority) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
    }

    public DepartmentDTO withRooms(List<RoomDTO> rooms) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
    }
}
