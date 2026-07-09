package org.acme.orderpicking.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The order picking planning problem output.")
public record OrderPickingOutput(
        @Schema(description = "List of trolleys with their assigned ordered pick tasks.") List<TrolleyDTO> trolleys,
        @Schema(description = "List of pick tasks of the problem.") List<PickTaskDTO> pickTasks,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public OrderPickingOutput {
        trolleys = List.copyOf(trolleys);
        pickTasks = List.copyOf(pickTasks);
    }

    public OrderPickingOutput withTrolleys(List<TrolleyDTO> trolleys) {
        return new OrderPickingOutput(trolleys, pickTasks, score);
    }

    public OrderPickingOutput withPickTasks(List<PickTaskDTO> pickTasks) {
        return new OrderPickingOutput(trolleys, pickTasks, score);
    }

    public OrderPickingOutput withScore(String score) {
        return new OrderPickingOutput(trolleys, pickTasks, score);
    }
}
