package org.acme.facilitylocation.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The facility location planning problem input.")
public record FacilityPlanInput(
        @Schema(description = "Candidate facilities that consumers can be served from.", required = true,
                minItems = 1) List<FacilityInputDTO> facilities,
        @Schema(description = "Consumers that each have to be served by one of the facilities.", required = true,
                minItems = 1) List<ConsumerInputDTO> consumers)
        implements
            ModelInput {

    public FacilityPlanInput withConsumers(List<ConsumerInputDTO> consumers) {
        return new FacilityPlanInput(facilities, consumers);
    }
}
