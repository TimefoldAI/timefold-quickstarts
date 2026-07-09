package org.acme.projectjobschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "One way of executing a job, with a specific duration and resource requirements.")
public record ExecutionModeDTO(
        @Schema(description = "Unique identifier of the execution mode.") String id,
        @Schema(description = "ID of the job this execution mode belongs to.") String jobId,
        @Schema(description = "Duration, in days, of the job when executed in this mode.") int duration) {

    public ExecutionModeDTO {
        id = id == null ? "" : id;
        jobId = jobId == null ? "" : jobId;
    }

    public ExecutionModeDTO withId(String id) {
        return new ExecutionModeDTO(id, jobId, duration);
    }

    public ExecutionModeDTO withJobId(String jobId) {
        return new ExecutionModeDTO(id, jobId, duration);
    }

    public ExecutionModeDTO withDuration(int duration) {
        return new ExecutionModeDTO(id, jobId, duration);
    }
}
