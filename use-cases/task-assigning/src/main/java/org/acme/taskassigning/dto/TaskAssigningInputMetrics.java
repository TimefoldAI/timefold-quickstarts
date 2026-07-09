package org.acme.taskassigning.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the task assigning problem submitted in the input dataset.")
public record TaskAssigningInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_EMPLOYEES, title = "Employees",
                format = DataFormat.Values.NUMBER,
                description = "The number of employees submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "8", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "8") }) int employees,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TASKS, title = "Tasks",
                format = DataFormat.Values.NUMBER,
                description = "The number of tasks submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "28", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "28") }) int tasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TASK_TYPES, title = "Task types",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct task types submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "4", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "4") }) int taskTypes,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_CUSTOMERS, title = "Customers",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct customers submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "4", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "4") }) int customers)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_EMPLOYEES = "employees";
    public static final String INPUT_METRIC_TASKS = "tasks";
    public static final String INPUT_METRIC_TASK_TYPES = "taskTypes";
    public static final String INPUT_METRIC_CUSTOMERS = "customers";

    public TaskAssigningInputMetrics {
        if (employees < 0 || tasks < 0 || taskTypes < 0 || customers < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public TaskAssigningInputMetrics withEmployees(int employees) {
        return new TaskAssigningInputMetrics(employees, tasks, taskTypes, customers);
    }

    public TaskAssigningInputMetrics withTasks(int tasks) {
        return new TaskAssigningInputMetrics(employees, tasks, taskTypes, customers);
    }

    public TaskAssigningInputMetrics withTaskTypes(int taskTypes) {
        return new TaskAssigningInputMetrics(employees, tasks, taskTypes, customers);
    }

    public TaskAssigningInputMetrics withCustomers(int customers) {
        return new TaskAssigningInputMetrics(employees, tasks, taskTypes, customers);
    }
}
