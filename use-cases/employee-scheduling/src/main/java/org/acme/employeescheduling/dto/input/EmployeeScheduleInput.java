package org.acme.employeescheduling.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The employee scheduling planning problem input.")
public record EmployeeScheduleInput(
        @Schema(description = "Employees who can work the shifts.", required = true,
                minItems = 1) List<EmployeeInputDTO> employees,
        @Schema(description = "Shifts to assign an employee to.", required = true,
                minItems = 1) List<ShiftInputDTO> shifts)
        implements
            ModelInput {

    public EmployeeScheduleInput withShifts(List<ShiftInputDTO> shifts) {
        return new EmployeeScheduleInput(employees, shifts);
    }
}
