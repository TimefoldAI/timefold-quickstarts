package org.acme.foodpackaging.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a line ID validation issue.")
public record LineIdDetail(
        @Schema(description = "The ID of the line.") String lineId) implements IssueMetadata {

    public LineIdDetail {
        lineId = lineId == null ? "" : lineId;
    }

    public LineIdDetail withLineId(String lineId) {
        return new LineIdDetail(lineId);
    }

    @Override
    public String getType() {
        return "LineId";
    }
}
