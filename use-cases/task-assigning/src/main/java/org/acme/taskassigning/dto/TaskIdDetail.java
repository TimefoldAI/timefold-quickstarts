package org.acme.taskassigning.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a task ID validation issue.")
public record TaskIdDetail(
        @Schema(description = "The ID of the task.") String taskId) implements IssueMetadata {

    public TaskIdDetail {
        taskId = taskId == null ? "" : taskId;
    }

    public TaskIdDetail withTaskId(String taskId) {
        return new TaskIdDetail(taskId);
    }

    @Override
    public String getType() {
        return "TaskId";
    }
}
