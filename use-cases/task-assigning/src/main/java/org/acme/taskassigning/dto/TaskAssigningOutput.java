package org.acme.taskassigning.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The task assigning planning problem output.")
public record TaskAssigningOutput(
        @Schema(description = "Employees with their assigned ordered task IDs.") List<EmployeeDTO> employees,
        @Schema(description = "Tasks with their computed start times.") List<TaskDTO> tasks,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public TaskAssigningOutput {
        employees = List.copyOf(employees);
        tasks = List.copyOf(tasks);
    }

    public TaskAssigningOutput withEmployees(List<EmployeeDTO> employees) {
        return new TaskAssigningOutput(employees, tasks, score);
    }

    public TaskAssigningOutput withTasks(List<TaskDTO> tasks) {
        return new TaskAssigningOutput(employees, tasks, score);
    }

    public TaskAssigningOutput withScore(String score) {
        return new TaskAssigningOutput(employees, tasks, score);
    }
}
