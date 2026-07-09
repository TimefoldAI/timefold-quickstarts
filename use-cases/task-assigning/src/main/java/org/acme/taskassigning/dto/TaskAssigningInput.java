package org.acme.taskassigning.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The task assigning planning problem input.")
public record TaskAssigningInput(
        @Schema(description = "Task types available in the problem.") List<TaskTypeDTO> taskTypes,
        @Schema(description = "Customers the tasks are performed for.") List<CustomerDTO> customers,
        @Schema(description = "Employees the tasks can be assigned to.") List<EmployeeDTO> employees,
        @Schema(description = "Tasks that must each be assigned to an employee.") List<TaskDTO> tasks)
        implements
            ModelInput {

    public TaskAssigningInput {
        taskTypes = List.copyOf(taskTypes);
        customers = List.copyOf(customers);
        employees = List.copyOf(employees);
        tasks = List.copyOf(tasks);
    }

    public TaskAssigningInput withTaskTypes(List<TaskTypeDTO> taskTypes) {
        return new TaskAssigningInput(taskTypes, customers, employees, tasks);
    }

    public TaskAssigningInput withCustomers(List<CustomerDTO> customers) {
        return new TaskAssigningInput(taskTypes, customers, employees, tasks);
    }

    public TaskAssigningInput withEmployees(List<EmployeeDTO> employees) {
        return new TaskAssigningInput(taskTypes, customers, employees, tasks);
    }

    public TaskAssigningInput withTasks(List<TaskDTO> tasks) {
        return new TaskAssigningInput(taskTypes, customers, employees, tasks);
    }
}
