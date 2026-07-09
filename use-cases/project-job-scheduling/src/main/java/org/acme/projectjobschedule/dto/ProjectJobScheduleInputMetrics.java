package org.acme.projectjobschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the project job scheduling problem submitted in the input dataset.")
public record ProjectJobScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_PROJECTS, title = "Projects",
                format = DataFormat.Values.NUMBER,
                description = "The number of projects submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "2", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "2") }) int projects,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_JOBS, title = "Jobs",
                format = DataFormat.Values.NUMBER,
                description = "The number of jobs submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int jobs,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_RESOURCES, title = "Resources",
                format = DataFormat.Values.NUMBER,
                description = "The number of resources submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "7", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "7") }) int resources,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_EXECUTION_MODES,
                title = "Execution modes", format = DataFormat.Values.NUMBER,
                description = "The number of execution modes submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "60", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "60") }) int executionModes,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ALLOCATIONS, title = "Allocations",
                format = DataFormat.Values.NUMBER,
                description = "The number of allocations submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int allocations)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_PROJECTS = "projects";
    public static final String INPUT_METRIC_JOBS = "jobs";
    public static final String INPUT_METRIC_RESOURCES = "resources";
    public static final String INPUT_METRIC_EXECUTION_MODES = "executionModes";
    public static final String INPUT_METRIC_ALLOCATIONS = "allocations";

    public ProjectJobScheduleInputMetrics {
        if (projects < 0 || jobs < 0 || resources < 0 || executionModes < 0 || allocations < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public ProjectJobScheduleInputMetrics withProjects(int projects) {
        return new ProjectJobScheduleInputMetrics(projects, jobs, resources, executionModes, allocations);
    }

    public ProjectJobScheduleInputMetrics withJobs(int jobs) {
        return new ProjectJobScheduleInputMetrics(projects, jobs, resources, executionModes, allocations);
    }

    public ProjectJobScheduleInputMetrics withResources(int resources) {
        return new ProjectJobScheduleInputMetrics(projects, jobs, resources, executionModes, allocations);
    }

    public ProjectJobScheduleInputMetrics withExecutionModes(int executionModes) {
        return new ProjectJobScheduleInputMetrics(projects, jobs, resources, executionModes, allocations);
    }

    public ProjectJobScheduleInputMetrics withAllocations(int allocations) {
        return new ProjectJobScheduleInputMetrics(projects, jobs, resources, executionModes, allocations);
    }
}
