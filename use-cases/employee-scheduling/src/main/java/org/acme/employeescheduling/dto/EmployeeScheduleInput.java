package org.acme.employeescheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The employee scheduling planning problem input.")
public record EmployeeScheduleInput(
        @Schema(description = "List of employees available to cover shifts.") List<EmployeeDTO> employees,
        @Schema(description = "List of shifts that must each be assigned to an employee.") List<ShiftDTO> shifts)
        implements
            ModelInput {

    public EmployeeScheduleInput {
        employees = List.copyOf(employees);
        shifts = List.copyOf(shifts);
    }

    public EmployeeScheduleInput withEmployees(List<EmployeeDTO> employees) {
        return new EmployeeScheduleInput(employees, shifts);
    }

    public EmployeeScheduleInput withShifts(List<ShiftDTO> shifts) {
        return new EmployeeScheduleInput(employees, shifts);
    }
}
