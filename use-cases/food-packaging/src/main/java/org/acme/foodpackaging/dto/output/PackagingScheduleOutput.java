package org.acme.foodpackaging.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The food packaging planning problem output.")
public record PackagingScheduleOutput(
        @Schema(description = "Lines with their assigned operator and job sequence.", required = true,
                minItems = 1) List<LineAssignmentDTO> lines,
        @Schema(description = "Jobs with the line they are produced on and their production times, if any.",
                required = true, minItems = 1) List<JobAssignmentDTO> jobs)
        implements
            ModelOutput {
}
