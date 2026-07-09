package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a bed ID validation issue.")
public record BedIdDetail(
        @Schema(description = "The ID of the bed.") String bedId) implements IssueMetadata {

    public BedIdDetail {
        bedId = bedId == null ? "" : bedId;
    }

    public BedIdDetail withBedId(String bedId) {
        return new BedIdDetail(bedId);
    }

    @Override
    public String getType() {
        return "BedId";
    }
}
