package org.acme.employeescheduling.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The employee scheduling planning problem output.")
public record EmployeeScheduleOutput(
        @Schema(description = "Shifts with the employee they are assigned to.", required = true,
                minItems = 1) List<ShiftOutputDTO> shifts)
        implements
            ModelOutput {
}
