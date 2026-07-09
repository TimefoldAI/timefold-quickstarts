package org.acme.facilitylocation.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The facility location planning problem input.")
public record FacilityLocationInput(
        @Schema(description = "List of facilities available to serve consumers.") List<FacilityDTO> facilities,
        @Schema(description = "List of consumers that must each be assigned to exactly one facility.") List<ConsumerDTO> consumers,
        @Schema(description = "The bounds of the problem.") List<LocationDTO> bounds) implements ModelInput {

    public FacilityLocationInput {
        facilities = List.copyOf(facilities);
        consumers = List.copyOf(consumers);
        bounds = bounds == null ? List.of() : List.copyOf(bounds);
    }

    public FacilityLocationInput(List<FacilityDTO> facilities, List<ConsumerDTO> consumers) {
        this(facilities, consumers, null);
    }

    public FacilityLocationInput withFacilities(List<FacilityDTO> facilities) {
        return new FacilityLocationInput(facilities, consumers, bounds);
    }

    public FacilityLocationInput withConsumers(List<ConsumerDTO> consumers) {
        return new FacilityLocationInput(facilities, consumers, bounds);
    }

    public FacilityLocationInput withBounds(List<LocationDTO> bounds) {
        return new FacilityLocationInput(facilities, consumers, bounds);
    }
}
