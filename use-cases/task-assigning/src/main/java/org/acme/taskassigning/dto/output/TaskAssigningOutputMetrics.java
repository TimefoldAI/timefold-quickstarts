package org.acme.taskassigning.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the task assignment produced for this dataset.")
public record TaskAssigningOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_TASKS,
                title = "Assigned tasks", format = DataFormat.Values.NUMBER,
                description = "The number of tasks assigned to an employee in this schedule.",
                type = SchemaType.INTEGER, examples = "28", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "28") }) int totalAssignedTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_TASKS,
                title = "Unassigned tasks", format = DataFormat.Values.NUMBER,
                description = "The number of tasks left unassigned in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedTasks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_EMPLOYEES, title = "Used employees",
                format = DataFormat.Values.NUMBER,
                description = "The number of employees assigned at least one task in this schedule.",
                type = SchemaType.INTEGER, examples = "8", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "8") }) int totalUsedEmployees,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = MAKESPAN_IN_MINUTES, title = "Makespan",
                format = DataFormat.Values.NUMBER,
                description = "The number of minutes until the last employee finishes their last task.",
                type = SchemaType.INTEGER, examples = "480", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "480") }) long makespanInMinutes)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_TASKS = "totalAssignedTasks";
    public static final String TOTAL_UNASSIGNED_TASKS = "totalUnassignedTasks";
    public static final String TOTAL_USED_EMPLOYEES = "totalUsedEmployees";
    public static final String MAKESPAN_IN_MINUTES = "makespanInMinutes";

    public TaskAssigningOutputMetrics {
        if (totalAssignedTasks < 0 || totalUnassignedTasks < 0 || totalUsedEmployees < 0 || makespanInMinutes < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedTasks (%d), totalUnassignedTasks (%d), totalUsedEmployees (%d), makespanInMinutes (%d)."
                            .formatted(totalAssignedTasks, totalUnassignedTasks, totalUsedEmployees,
                                    makespanInMinutes));
        }
    }
}
