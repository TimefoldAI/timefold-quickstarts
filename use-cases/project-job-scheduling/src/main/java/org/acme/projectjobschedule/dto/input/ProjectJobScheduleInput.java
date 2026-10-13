package org.acme.projectjobschedule.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The project job scheduling planning problem input.")
public record ProjectJobScheduleInput(
        @Schema(description = "Projects whose jobs have to be scheduled.", required = true,
                minItems = 1) List<ProjectInputDTO> projects,
        @Schema(description = "Resources the jobs consume while they execute.", required = true,
                minItems = 1) List<ResourceInputDTO> resources,
        @Schema(description = "Jobs that make up the projects.", required = true, minItems = 1) List<JobInputDTO> jobs,
        @Schema(description = "One allocation per job, holding the schedule of that job.", required = true,
                minItems = 1) List<AllocationInputDTO> allocations)
        implements
            ModelInput {

    public ProjectJobScheduleInput withAllocations(List<AllocationInputDTO> allocations) {
        return new ProjectJobScheduleInput(projects, resources, jobs, allocations);
    }
}
