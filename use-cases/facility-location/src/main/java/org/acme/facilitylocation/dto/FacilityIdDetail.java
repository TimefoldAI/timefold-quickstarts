package org.acme.facilitylocation.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a facility ID validation issue.")
public record FacilityIdDetail(
        @Schema(description = "The ID of the facility.") String facilityId) implements IssueMetadata {

    public FacilityIdDetail {
        facilityId = facilityId == null ? "" : facilityId;
    }

    public FacilityIdDetail withFacilityId(String facilityId) {
        return new FacilityIdDetail(facilityId);
    }

    @Override
    public String getType() {
        return "FacilityId";
    }
}
