package org.acme.projectjobschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The project job scheduling planning problem input.")
public record ProjectJobScheduleInput(
        @Schema(description = "List of projects to schedule.") List<ProjectDTO> projects,
        @Schema(description = "List of resources jobs consume.") List<ResourceDTO> resources,
        @Schema(description = "List of jobs belonging to the projects.") List<JobDTO> jobs,
        @Schema(description = "List of execution modes for the jobs.") List<ExecutionModeDTO> executionModes,
        @Schema(description = "List of resource requirements of the execution modes.") List<ResourceRequirementDTO> resourceRequirements,
        @Schema(description = "List of allocations to schedule.") List<AllocationDTO> allocations) implements ModelInput {

    public ProjectJobScheduleInput {
        projects = List.copyOf(projects);
        resources = List.copyOf(resources);
        jobs = List.copyOf(jobs);
        executionModes = List.copyOf(executionModes);
        resourceRequirements = List.copyOf(resourceRequirements);
        allocations = List.copyOf(allocations);
    }

    public ProjectJobScheduleInput withProjects(List<ProjectDTO> projects) {
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }

    public ProjectJobScheduleInput withResources(List<ResourceDTO> resources) {
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }

    public ProjectJobScheduleInput withJobs(List<JobDTO> jobs) {
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }

    public ProjectJobScheduleInput withExecutionModes(List<ExecutionModeDTO> executionModes) {
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }

    public ProjectJobScheduleInput withResourceRequirements(List<ResourceRequirementDTO> resourceRequirements) {
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }

    public ProjectJobScheduleInput withAllocations(List<AllocationDTO> allocations) {
        return new ProjectJobScheduleInput(projects, resources, jobs, executionModes, resourceRequirements, allocations);
    }
}
