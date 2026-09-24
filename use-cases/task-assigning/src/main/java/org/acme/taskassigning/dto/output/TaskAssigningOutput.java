package org.acme.taskassigning.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The task assigning planning problem output.")
public record TaskAssigningOutput(
        @Schema(description = "Employees with the tasks assigned to them, if any.",
                required = true) List<EmployeeOutputDTO> employees)
        implements
            ModelOutput {
}
