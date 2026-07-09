package org.acme.vehiclerouting.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The vehicle routing planning problem output.")
public record VehicleRoutingOutput(
        @Schema(description = "Vehicles with their assigned ordered visit IDs and computed route metrics.") List<VehicleDTO> vehicles,
        @Schema(description = "Visits with their computed arrival and service times.") List<VisitDTO> visits,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public VehicleRoutingOutput {
        vehicles = List.copyOf(vehicles);
        visits = List.copyOf(visits);
    }

    public VehicleRoutingOutput withVehicles(List<VehicleDTO> vehicles) {
        return new VehicleRoutingOutput(vehicles, visits, score);
    }

    public VehicleRoutingOutput withVisits(List<VisitDTO> visits) {
        return new VehicleRoutingOutput(vehicles, visits, score);
    }

    public VehicleRoutingOutput withScore(String score) {
        return new VehicleRoutingOutput(vehicles, visits, score);
    }
}
