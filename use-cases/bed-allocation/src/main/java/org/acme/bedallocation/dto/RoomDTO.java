package org.acme.bedallocation.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room within a department that contains one or more beds.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.") String id,
        @Schema(description = "Display name of the room.") String name,
        @Schema(description = "ID of the department the room belongs to.") String departmentId,
        @Schema(description = "Number of beds the room can hold.") int capacity,
        @Schema(description = "Gender limitation that applies to the room, e.g. MALE_ONLY or SAME_GENDER.") String genderLimitation,
        @Schema(description = "Equipment available in the room.") List<String> equipments) {

    public RoomDTO {
        name = name == null ? "" : name;
        departmentId = normalize(departmentId);
        genderLimitation = normalize(genderLimitation);
        equipments = equipments == null ? List.of() : List.copyOf(equipments);
    }

    private static String normalize(String value) {
        return value != null && value.isBlank() ? null : value;
    }

    public RoomDTO withId(String id) {
        return new RoomDTO(id, name, departmentId, capacity, genderLimitation, equipments);
    }

    public RoomDTO withName(String name) {
        return new RoomDTO(id, name, departmentId, capacity, genderLimitation, equipments);
    }

    public RoomDTO withDepartmentId(String departmentId) {
        return new RoomDTO(id, name, departmentId, capacity, genderLimitation, equipments);
    }

    public RoomDTO withCapacity(int capacity) {
        return new RoomDTO(id, name, departmentId, capacity, genderLimitation, equipments);
    }

    public RoomDTO withGenderLimitation(String genderLimitation) {
        return new RoomDTO(id, name, departmentId, capacity, genderLimitation, equipments);
    }

    public RoomDTO withEquipments(List<String> equipments) {
        return new RoomDTO(id, name, departmentId, capacity, genderLimitation, equipments);
    }
}
