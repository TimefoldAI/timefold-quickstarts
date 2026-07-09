package org.acme.vehiclerouting.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a visit ID validation issue.")
public record VisitIdDetail(
        @Schema(description = "The ID of the visit.") String visitId) implements IssueMetadata {

    public VisitIdDetail {
        visitId = visitId == null ? "" : visitId;
    }

    public VisitIdDetail withVisitId(String visitId) {
        return new VisitIdDetail(visitId);
    }

    @Override
    public String getType() {
        return "VisitId";
    }
}
