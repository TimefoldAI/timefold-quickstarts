package org.acme.foodpackaging.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a job ID validation issue.")
public record JobIdDetail(
        @Schema(description = "The ID of the job.") String jobId) implements IssueMetadata {

    public JobIdDetail {
        jobId = jobId == null ? "" : jobId;
    }

    public JobIdDetail withJobId(String jobId) {
        return new JobIdDetail(jobId);
    }

    @Override
    public String getType() {
        return "JobId";
    }
}
