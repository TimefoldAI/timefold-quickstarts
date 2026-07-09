package org.acme.taskassigning.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A type of task that can be assigned to employees, with its base duration and required skills.")
public record TaskTypeDTO(
        @Schema(description = "Unique code of the task type.") String code,
        @Schema(description = "Human readable title of the task type.") String title,
        @Schema(description = "Base duration in minutes before affinity is applied.") long baseDuration,
        @Schema(description = "Skills required to perform a task of this type.") List<String> requiredSkills) {

    public TaskTypeDTO {
        code = code == null ? "" : code;
        title = title == null ? "" : title;
        requiredSkills = List.copyOf(requiredSkills);
    }

    public TaskTypeDTO withCode(String code) {
        return new TaskTypeDTO(code, title, baseDuration, requiredSkills);
    }

    public TaskTypeDTO withTitle(String title) {
        return new TaskTypeDTO(code, title, baseDuration, requiredSkills);
    }

    public TaskTypeDTO withBaseDuration(long baseDuration) {
        return new TaskTypeDTO(code, title, baseDuration, requiredSkills);
    }

    public TaskTypeDTO withRequiredSkills(List<String> requiredSkills) {
        return new TaskTypeDTO(code, title, baseDuration, requiredSkills);
    }
}
