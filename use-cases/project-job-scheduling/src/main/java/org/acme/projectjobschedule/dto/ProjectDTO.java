package org.acme.projectjobschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A project consisting of jobs that must be scheduled.")
public record ProjectDTO(
        @Schema(description = "Unique identifier of the project.") String id,
        @Schema(description = "The earliest date, in days, at which the project may start.") int releaseDate,
        @Schema(description = "The duration, in days, of the project's critical path.") int criticalPathDuration) {

    public ProjectDTO {
        id = id == null ? "" : id;
    }

    public ProjectDTO withId(String id) {
        return new ProjectDTO(id, releaseDate, criticalPathDuration);
    }

    public ProjectDTO withReleaseDate(int releaseDate) {
        return new ProjectDTO(id, releaseDate, criticalPathDuration);
    }

    public ProjectDTO withCriticalPathDuration(int criticalPathDuration) {
        return new ProjectDTO(id, releaseDate, criticalPathDuration);
    }
}
