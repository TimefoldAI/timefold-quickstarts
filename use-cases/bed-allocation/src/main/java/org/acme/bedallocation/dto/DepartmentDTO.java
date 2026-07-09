package org.acme.bedallocation.dto;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A hospital department that groups rooms and constrains the patients staying in them.")
public record DepartmentDTO(
        @Schema(description = "Unique identifier of the department.") String id,
        @Schema(description = "Display name of the department.") String name,
        @Schema(description = "Minimum patient age allowed in this department. Null when there is no lower bound.") Integer minimumAge,
        @Schema(description = "Maximum patient age allowed in this department. Null when there is no upper bound.") Integer maximumAge,
        @Schema(description = "Mapping of specialty name to its priority within this department.") Map<String, Integer> specialtyToPriority) {

    public DepartmentDTO {
        name = name == null ? "" : name;
        specialtyToPriority = specialtyToPriority == null ? Map.of() : Map.copyOf(specialtyToPriority);
    }

    public DepartmentDTO withId(String id) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority);
    }

    public DepartmentDTO withName(String name) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority);
    }

    public DepartmentDTO withMinimumAge(Integer minimumAge) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority);
    }

    public DepartmentDTO withMaximumAge(Integer maximumAge) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority);
    }

    public DepartmentDTO withSpecialtyToPriority(Map<String, Integer> specialtyToPriority) {
        return new DepartmentDTO(id, name, minimumAge, maximumAge, specialtyToPriority);
    }
}
