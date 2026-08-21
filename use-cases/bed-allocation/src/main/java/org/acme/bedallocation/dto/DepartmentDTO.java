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
        specialtyToPriority = specialtyToPriority == null ? Map.of() : Map.copyOf(specialtyToPriority);
        rooms = rooms == null ? List.of() : List.copyOf(rooms);
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public static final class Builder {

        private final String id;
        private final String name;
        private Integer minimumAge;
        private Integer maximumAge;
        private Map<String, Integer> specialtyToPriority;
        private List<RoomDTO> rooms;

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder minimumAge(Integer minimumAge) {
            this.minimumAge = minimumAge;
            return this;
        }

        public Builder maximumAge(Integer maximumAge) {
            this.maximumAge = maximumAge;
            return this;
        }

        public Builder specialtyToPriority(Map<String, Integer> specialtyToPriority) {
            this.specialtyToPriority = specialtyToPriority;
            return this;
        }

        public Builder rooms(List<RoomDTO> rooms) {
            this.rooms = rooms;
            return this;
        }

        public DepartmentDTO build() {
            return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority, rooms);
        }
    }
}
