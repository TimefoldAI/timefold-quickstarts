package org.acme.orderpicking.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The order picking planning problem output.")
public record PickingPlanOutput(
        @Schema(description = "Picking carts with the pick tasks they pick, in order.", required = true,
                minItems = 1) List<PickingCartOutputDTO> pickingCarts)
        implements
            ModelOutput {
}
