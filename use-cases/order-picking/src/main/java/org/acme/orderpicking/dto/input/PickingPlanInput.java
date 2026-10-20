package org.acme.orderpicking.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The order picking planning problem input.")
public record PickingPlanInput(
        @Schema(description = "Products stored in the warehouse that pick tasks refer to.", required = true,
                minItems = 1) List<ProductInputDTO> products,
        @Schema(description = "Pick tasks to assign to a picking cart, one per order item.", required = true,
                minItems = 1) List<PickTaskInputDTO> pickTasks,
        @Schema(description = "Picking carts that pick the order items.", required = true,
                minItems = 1) List<PickingCartInputDTO> pickingCarts)
        implements
            ModelInput {

    public PickingPlanInput withPickingCarts(List<PickingCartInputDTO> pickingCarts) {
        return new PickingPlanInput(products, pickTasks, pickingCarts);
    }
}
