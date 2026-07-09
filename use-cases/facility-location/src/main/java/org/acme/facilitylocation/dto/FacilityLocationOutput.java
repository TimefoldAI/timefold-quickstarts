package org.acme.facilitylocation.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The facility location planning problem output.")
public record FacilityLocationOutput(
        @Schema(description = "List of facilities available to serve consumers.") List<FacilityDTO> facilities,
        @Schema(description = "List of consumers that must each be assigned to exactly one facility.") List<ConsumerDTO> consumers,
        @Schema(description = "The score of the solution.") String score,
        @Schema(description = "The total cost of the solution.") long totalCost,
        @Schema(description = "The potential cost of the solution.") long potentialCost,
        @Schema(description = "The total distance of the solution.") String totalDistance,
        @Schema(description = "The bounds of the problem.") List<LocationDTO> bounds) implements ModelOutput {

    public FacilityLocationOutput {
        facilities = List.copyOf(facilities);
        consumers = List.copyOf(consumers);
        bounds = bounds == null ? List.of() : List.copyOf(bounds);
    }

    public FacilityLocationOutput withFacilities(List<FacilityDTO> facilities) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }

    public FacilityLocationOutput withConsumers(List<ConsumerDTO> consumers) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }

    public FacilityLocationOutput withScore(String score) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }

    public FacilityLocationOutput withTotalCost(long totalCost) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }

    public FacilityLocationOutput withPotentialCost(long potentialCost) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }

    public FacilityLocationOutput withTotalDistance(String totalDistance) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }

    public FacilityLocationOutput withBounds(List<LocationDTO> bounds) {
        return new FacilityLocationOutput(facilities, consumers, score, totalCost, potentialCost, totalDistance, bounds);
    }
}
