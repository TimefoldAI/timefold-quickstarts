package org.acme.projectjobschedule.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The project job scheduling planning problem output.")
public record ProjectJobScheduleOutput(
        @Schema(description = "One allocation per job, holding the schedule of that job.", required = true,
                minItems = 1) List<AllocationOutputDTO> allocations)
        implements
            ModelOutput {
}
