package org.acme.taskassigning.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single task to be assigned to an employee.")
public record TaskDTO(
        @Schema(description = "Unique identifier of the task.") String id,
        @Schema(description = "Code of the task type this task belongs to.") String taskTypeCode,
        @Schema(description = "Index of the task within its task type.") int indexInTaskType,
        @Schema(description = "Identifier of the customer this task is for.") String customerId,
        @Schema(description = "Earliest time in minutes at which the task may start.") long minStartTime,
        @Schema(description = "Priority of the task: MINOR, MAJOR or CRITICAL.") String priority,
        @Schema(description = "Computed start time in minutes, or null when the task is unassigned.") Long startTime) {

    public TaskDTO {
        id = id == null ? "" : id;
        taskTypeCode = taskTypeCode == null ? "" : taskTypeCode;
        customerId = customerId == null ? "" : customerId;
        priority = priority == null ? "" : priority;
    }

    public TaskDTO withId(String id) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }

    public TaskDTO withTaskTypeCode(String taskTypeCode) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }

    public TaskDTO withIndexInTaskType(int indexInTaskType) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }

    public TaskDTO withCustomerId(String customerId) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }

    public TaskDTO withMinStartTime(long minStartTime) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }

    public TaskDTO withPriority(String priority) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }

    public TaskDTO withStartTime(Long startTime) {
        return new TaskDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTime, priority, startTime);
    }
}
