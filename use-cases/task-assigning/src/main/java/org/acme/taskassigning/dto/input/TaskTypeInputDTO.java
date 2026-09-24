package org.acme.taskassigning.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A kind of task, shared by every task of that kind.")
public record TaskTypeInputDTO(
        @Schema(description = "Unique identifier of the task type.", required = true, minLength = 1) String code,
        @Schema(description = "Human-readable title of the task type.", required = true, minLength = 1) String title,
        @Schema(description = "How long a task of this type takes with no affinity discount, in minutes.",
                required = true, minimum = "1") Integer baseDurationInMinutes,
        @Schema(description = "The skills an employee must have to be assigned a task of this type.") List<String> requiredSkills) {

    public TaskTypeInputDTO {
        requiredSkills = requiredSkills != null ? requiredSkills : emptyList();
    }
}
