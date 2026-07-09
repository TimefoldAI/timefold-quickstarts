package org.acme.orderpicking.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a trolley ID validation issue.")
public record TrolleyIdDetail(
        @Schema(description = "The ID of the trolley.") String trolleyId) implements IssueMetadata {

    public TrolleyIdDetail {
        trolleyId = trolleyId == null ? "" : trolleyId;
    }

    public TrolleyIdDetail withTrolleyId(String trolleyId) {
        return new TrolleyIdDetail(trolleyId);
    }

    @Override
    public String getType() {
        return "TrolleyId";
    }
}
