package org.acme.orderpicking.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The order picking planning problem input.")
public record OrderPickingInput(
        @Schema(description = "List of trolleys available to pick the order items.") List<TrolleyDTO> trolleys,
        @Schema(description = "List of pick tasks that must each be assigned to a trolley.") List<PickTaskDTO> pickTasks)
        implements
            ModelInput {

    public OrderPickingInput {
        trolleys = List.copyOf(trolleys);
        pickTasks = List.copyOf(pickTasks);
    }

    public OrderPickingInput withTrolleys(List<TrolleyDTO> trolleys) {
        return new OrderPickingInput(trolleys, pickTasks);
    }

    public OrderPickingInput withPickTasks(List<PickTaskDTO> pickTasks) {
        return new OrderPickingInput(trolleys, pickTasks);
    }
}
