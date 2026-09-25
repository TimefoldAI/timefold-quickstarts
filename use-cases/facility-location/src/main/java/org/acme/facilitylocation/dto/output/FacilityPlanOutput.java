package org.acme.facilitylocation.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The facility location planning problem output.")
public record FacilityPlanOutput(
        @Schema(description = "Consumers with the facility serving them, if any.",
                required = true) List<ConsumerOutputDTO> consumers,
        @Schema(description = "Facilities with the load this solution puts on them.",
                required = true) List<FacilityOutputDTO> facilities)
        implements
            ModelOutput {
}
