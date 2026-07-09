package org.acme.employeescheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The employee scheduling planning problem output.")
public record EmployeeScheduleOutput(
        @Schema(description = "List of employees with their assigned shifts.") List<EmployeeDTO> employees,
        @Schema(description = "List of shifts with their assigned employee.") List<ShiftDTO> shifts,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public EmployeeScheduleOutput {
        employees = List.copyOf(employees);
        shifts = List.copyOf(shifts);
    }

    public EmployeeScheduleOutput withEmployees(List<EmployeeDTO> employees) {
        return new EmployeeScheduleOutput(employees, shifts, score);
    }

    public EmployeeScheduleOutput withShifts(List<ShiftDTO> shifts) {
        return new EmployeeScheduleOutput(employees, shifts, score);
    }

    public EmployeeScheduleOutput withScore(String score) {
        return new EmployeeScheduleOutput(employees, shifts, score);
    }
}
