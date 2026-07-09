package org.acme.projectjobschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The amount of a resource required by an execution mode.")
public record ResourceRequirementDTO(
        @Schema(description = "Unique identifier of the resource requirement.") String id,
        @Schema(description = "ID of the execution mode that requires the resource.") String executionModeId,
        @Schema(description = "ID of the required resource.") String resourceId,
        @Schema(description = "The amount of the resource required.") int requirement) {

    public ResourceRequirementDTO {
        id = id == null ? "" : id;
        executionModeId = executionModeId == null ? "" : executionModeId;
        resourceId = resourceId == null ? "" : resourceId;
    }

    public ResourceRequirementDTO withId(String id) {
        return new ResourceRequirementDTO(id, executionModeId, resourceId, requirement);
    }

    public ResourceRequirementDTO withExecutionModeId(String executionModeId) {
        return new ResourceRequirementDTO(id, executionModeId, resourceId, requirement);
    }

    public ResourceRequirementDTO withResourceId(String resourceId) {
        return new ResourceRequirementDTO(id, executionModeId, resourceId, requirement);
    }

    public ResourceRequirementDTO withRequirement(int requirement) {
        return new ResourceRequirementDTO(id, executionModeId, resourceId, requirement);
    }
}
