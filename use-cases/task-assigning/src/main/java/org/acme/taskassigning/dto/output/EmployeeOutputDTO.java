package org.acme.taskassigning.dto.output;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "An employee with the tasks assigned to them, in the order they are worked on.")
public record EmployeeOutputDTO(
        @Schema(description = "Unique identifier of the employee.", required = true, minLength = 1) String id,
        @Schema(description = "The tasks assigned to this employee, in the order they are worked on.",
                required = true) List<AssignedTaskOutputDTO> assignedTasks) {
}
