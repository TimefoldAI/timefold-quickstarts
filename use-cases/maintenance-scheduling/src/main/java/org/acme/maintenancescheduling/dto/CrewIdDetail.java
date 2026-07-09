package org.acme.maintenancescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a crew ID validation issue.")
public record CrewIdDetail(
        @Schema(description = "The ID of the crew.") String crewId) implements IssueMetadata {

    public CrewIdDetail {
        crewId = crewId == null ? "" : crewId;
    }

    public CrewIdDetail withCrewId(String crewId) {
        return new CrewIdDetail(crewId);
    }

    @Override
    public String getType() {
        return "CrewId";
    }
}
