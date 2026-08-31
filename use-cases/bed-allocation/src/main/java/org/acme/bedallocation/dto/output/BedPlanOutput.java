package org.acme.bedallocation.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem output.")
public record BedPlanOutput(
        @Schema(description = "Patient stays with their assigned bed, if any.", required = true) List<StayOutputDTO> stays)
        implements
            ModelOutput {
}
