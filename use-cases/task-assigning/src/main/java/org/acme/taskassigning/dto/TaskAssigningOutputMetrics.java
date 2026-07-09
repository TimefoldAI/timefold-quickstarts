package org.acme.taskassigning.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the task assigning solution produced for this dataset.")
public record TaskAssigningOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_TASKS, title = "Assigned tasks",
                format = DataFormat.Values.NUMBER,
                description = "The number of tasks assigned to an employee in this solution.",
                type = SchemaType.INTEGER, example = "28", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "28") }) int assignedTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_TASKS, title = "Unassigned tasks",
                format = DataFormat.Values.NUMBER,
                description = "The number of tasks left without an employee in this solution.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int unassignedTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_EMPLOYEES, title = "Used employees",
                format = DataFormat.Values.NUMBER,
                description = "The number of employees with at least one assigned task in this solution.",
                type = SchemaType.INTEGER, example = "8", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "8") }) int usedEmployees,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = MAKESPAN, title = "Makespan",
                format = DataFormat.Values.NUMBER,
                description = "The latest task end time in minutes across all employees in this solution.",
                type = SchemaType.INTEGER, example = "300", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "300") }) long makespan)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_TASKS = "assignedTasks";
    public static final String TOTAL_UNASSIGNED_TASKS = "unassignedTasks";
    public static final String TOTAL_USED_EMPLOYEES = "usedEmployees";
    public static final String MAKESPAN = "makespan";

    public TaskAssigningOutputMetrics {
        if (assignedTasks < 0 || unassignedTasks < 0 || usedEmployees < 0 || makespan < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public TaskAssigningOutputMetrics withAssignedTasks(int assignedTasks) {
        return new TaskAssigningOutputMetrics(assignedTasks, unassignedTasks, usedEmployees, makespan);
    }

    public TaskAssigningOutputMetrics withUnassignedTasks(int unassignedTasks) {
        return new TaskAssigningOutputMetrics(assignedTasks, unassignedTasks, usedEmployees, makespan);
    }

    public TaskAssigningOutputMetrics withUsedEmployees(int usedEmployees) {
        return new TaskAssigningOutputMetrics(assignedTasks, unassignedTasks, usedEmployees, makespan);
    }

    public TaskAssigningOutputMetrics withMakespan(long makespan) {
        return new TaskAssigningOutputMetrics(assignedTasks, unassignedTasks, usedEmployees, makespan);
    }
}
