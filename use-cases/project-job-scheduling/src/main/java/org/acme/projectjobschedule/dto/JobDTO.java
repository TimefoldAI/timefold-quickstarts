package org.acme.projectjobschedule.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A job that belongs to a project and must be scheduled through an execution mode.")
public record JobDTO(
        @Schema(description = "Unique identifier of the job.") String id,
        @Schema(description = "ID of the project the job belongs to.") String projectId,
        @Schema(description = "Type of the job: SOURCE, STANDARD or SINK.") String jobType,
        @Schema(description = "IDs of the jobs that must be completed before this job's successors.") List<String> successorJobIds) {

    public JobDTO {
        id = id == null ? "" : id;
        projectId = projectId == null ? "" : projectId;
        jobType = jobType == null ? "" : jobType;
        successorJobIds = successorJobIds == null ? List.of() : List.copyOf(successorJobIds);
    }

    public JobDTO withId(String id) {
        return new JobDTO(id, projectId, jobType, successorJobIds);
    }

    public JobDTO withProjectId(String projectId) {
        return new JobDTO(id, projectId, jobType, successorJobIds);
    }

    public JobDTO withJobType(String jobType) {
        return new JobDTO(id, projectId, jobType, successorJobIds);
    }

    public JobDTO withSuccessorJobIds(List<String> successorJobIds) {
        return new JobDTO(id, projectId, jobType, successorJobIds);
    }
}
