package org.acme.maintenancescheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A crew that can be assigned to maintenance jobs.")
public record CrewDTO(
        @Schema(description = "Unique identifier of the crew.") String id,
        @Schema(description = "Display name of the crew.") String name) {

    public CrewDTO {
        name = name == null ? "" : name;
    }

    public CrewDTO withId(String id) {
        return new CrewDTO(id, name);
    }

    public CrewDTO withName(String name) {
        return new CrewDTO(id, name);
    }
}
