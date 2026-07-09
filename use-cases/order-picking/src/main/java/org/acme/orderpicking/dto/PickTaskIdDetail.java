package org.acme.orderpicking.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a pick task ID validation issue.")
public record PickTaskIdDetail(
        @Schema(description = "The ID of the pick task.") String pickTaskId) implements IssueMetadata {

    public PickTaskIdDetail {
        pickTaskId = pickTaskId == null ? "" : pickTaskId;
    }

    public PickTaskIdDetail withPickTaskId(String pickTaskId) {
        return new PickTaskIdDetail(pickTaskId);
    }

    @Override
    public String getType() {
        return "PickTaskId";
    }
}
