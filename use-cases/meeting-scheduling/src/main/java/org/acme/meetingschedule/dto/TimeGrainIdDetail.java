package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a time grain ID validation issue.")
public record TimeGrainIdDetail(
        @Schema(description = "The ID of the time grain.") String timeGrainId) implements IssueMetadata {

    public TimeGrainIdDetail {
        timeGrainId = timeGrainId == null ? "" : timeGrainId;
    }

    public TimeGrainIdDetail withTimeGrainId(String timeGrainId) {
        return new TimeGrainIdDetail(timeGrainId);
    }

    @Override
    public String getType() {
        return "TimeGrainId";
    }
}
