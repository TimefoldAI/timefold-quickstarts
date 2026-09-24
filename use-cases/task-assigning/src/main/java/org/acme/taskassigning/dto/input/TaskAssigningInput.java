package org.acme.taskassigning.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The task assigning planning problem input.")
public record TaskAssigningInput(
        @Schema(description = "Customers whose tasks need to be handled.", required = true,
                minItems = 1) List<CustomerInputDTO> customers,
        @Schema(description = "The kinds of tasks that can be assigned.", required = true,
                minItems = 1) List<TaskTypeInputDTO> taskTypes,
        @Schema(description = "Employees who can be assigned tasks.", required = true,
                minItems = 1) List<EmployeeInputDTO> employees,
        @Schema(description = "Tasks that must each be assigned to an employee.", required = true,
                minItems = 1) List<TaskInputDTO> tasks)
        implements
            ModelInput {

    public TaskAssigningInput withEmployees(List<EmployeeInputDTO> employees) {
        return new TaskAssigningInput(customers, taskTypes, employees, tasks);
    }
}
