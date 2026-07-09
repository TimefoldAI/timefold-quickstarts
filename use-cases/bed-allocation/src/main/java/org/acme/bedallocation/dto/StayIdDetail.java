package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a stay ID validation issue.")
public record StayIdDetail(
        @Schema(description = "The ID of the stay.") String stayId) implements IssueMetadata {

    public StayIdDetail {
        stayId = stayId == null ? "" : stayId;
    }

    public StayIdDetail withStayId(String stayId) {
        return new StayIdDetail(stayId);
    }

    @Override
    public String getType() {
        return "StayId";
    }
}
