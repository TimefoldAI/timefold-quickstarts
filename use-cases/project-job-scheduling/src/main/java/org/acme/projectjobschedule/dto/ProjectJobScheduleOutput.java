package org.acme.projectjobschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The project job scheduling planning problem output.")
public record ProjectJobScheduleOutput(
        @Schema(description = "List of projects to schedule.") List<ProjectDTO> projects,
        @Schema(description = "List of resources jobs consume.") List<ResourceDTO> resources,
        @Schema(description = "List of jobs belonging to the projects.") List<JobDTO> jobs,
        @Schema(description = "List of execution modes for the jobs.") List<ExecutionModeDTO> executionModes,
        @Schema(description = "List of resource requirements of the execution modes.") List<ResourceRequirementDTO> resourceRequirements,
        @Schema(description = "List of allocations with their assigned execution mode, delay and dates.") List<AllocationDTO> allocations,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public ProjectJobScheduleOutput {
        projects = List.copyOf(projects);
        resources = List.copyOf(resources);
        jobs = List.copyOf(jobs);
        executionModes = List.copyOf(executionModes);
        resourceRequirements = List.copyOf(resourceRequirements);
        allocations = List.copyOf(allocations);
    }

    public ProjectJobScheduleOutput withProjects(List<ProjectDTO> projects) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    public ProjectJobScheduleOutput withResources(List<ResourceDTO> resources) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    public ProjectJobScheduleOutput withJobs(List<JobDTO> jobs) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    public ProjectJobScheduleOutput withExecutionModes(List<ExecutionModeDTO> executionModes) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    public ProjectJobScheduleOutput withResourceRequirements(List<ResourceRequirementDTO> resourceRequirements) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    public ProjectJobScheduleOutput withAllocations(List<AllocationDTO> allocations) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }

    public ProjectJobScheduleOutput withScore(String score) {
        return new ProjectJobScheduleOutput(projects, resources, jobs, executionModes, resourceRequirements, allocations,
                score);
    }
}
