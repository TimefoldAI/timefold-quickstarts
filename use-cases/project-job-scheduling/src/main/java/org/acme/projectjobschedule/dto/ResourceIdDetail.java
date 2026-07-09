package org.acme.projectjobschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a resource ID validation issue.")
public record ResourceIdDetail(
        @Schema(description = "The ID of the resource.") String resourceId) implements IssueMetadata {

    public ResourceIdDetail {
        resourceId = resourceId == null ? "" : resourceId;
    }

    public ResourceIdDetail withResourceId(String resourceId) {
        return new ResourceIdDetail(resourceId);
    }

    @Override
    public String getType() {
        return "ResourceId";
    }
}
