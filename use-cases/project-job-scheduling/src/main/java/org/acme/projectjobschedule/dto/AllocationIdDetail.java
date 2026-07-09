package org.acme.projectjobschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about an allocation ID validation issue.")
public record AllocationIdDetail(
        @Schema(description = "The ID of the allocation.") String allocationId) implements IssueMetadata {

    public AllocationIdDetail {
        allocationId = allocationId == null ? "" : allocationId;
    }

    public AllocationIdDetail withAllocationId(String allocationId) {
        return new AllocationIdDetail(allocationId);
    }

    @Override
    public String getType() {
        return "AllocationId";
    }
}
