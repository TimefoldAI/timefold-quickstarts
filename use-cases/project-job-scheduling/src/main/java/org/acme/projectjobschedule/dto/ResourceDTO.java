package org.acme.projectjobschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A resource with a limited capacity that jobs consume while executing.")
public record ResourceDTO(
        @Schema(description = "Unique identifier of the resource.") String id,
        @Schema(description = "Type of the resource: GLOBAL or LOCAL.") String resourceType,
        @Schema(description = "Total capacity of the resource.") int capacity,
        @Schema(description = "Whether the resource is renewable (capacity applies per day).") boolean renewable,
        @Schema(description = "ID of the project a local resource belongs to. Blank for global resources.") String projectId) {

    public ResourceDTO {
        id = id == null ? "" : id;
        resourceType = resourceType == null ? "" : resourceType;
        projectId = normalizeId(projectId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public ResourceDTO withId(String id) {
        return new ResourceDTO(id, resourceType, capacity, renewable, projectId);
    }

    public ResourceDTO withResourceType(String resourceType) {
        return new ResourceDTO(id, resourceType, capacity, renewable, projectId);
    }

    public ResourceDTO withCapacity(int capacity) {
        return new ResourceDTO(id, resourceType, capacity, renewable, projectId);
    }

    public ResourceDTO withRenewable(boolean renewable) {
        return new ResourceDTO(id, resourceType, capacity, renewable, projectId);
    }

    public ResourceDTO withProjectId(String projectId) {
        return new ResourceDTO(id, resourceType, capacity, renewable, projectId);
    }
}
