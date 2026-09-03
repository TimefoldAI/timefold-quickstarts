package org.acme.taskassigning.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A task assigned to an employee, at the position it is worked on in.")
public record AssignedTaskOutputDTO(
        @Schema(description = "Unique identifier of the task.", required = true, minLength = 1) String taskId,
        @Schema(description = "The minute this task starts on, relative to the start of the schedule.",
                required = true) long startTimeInMinutes) {
}
