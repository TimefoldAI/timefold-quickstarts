package org.acme.facilitylocation.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A facility that can serve consumers up to its capacity at a given setup cost.")
public record FacilityDTO(
        @Schema(description = "Unique identifier of the facility.") String id,
        @Schema(description = "Name of the facility.") String name,
        @Schema(description = "Geographic location of the facility.") LocationDTO location,
        @Schema(description = "One-time cost incurred when this facility is used by at least one consumer.") long setupCost,
        @Schema(description = "Maximum total demand this facility can serve across all assigned consumers.") long capacity,
        @Schema(description = "Total demand of all consumers currently assigned to this facility.") long usedCapacity,
        @Schema(description = "True if at least one consumer is assigned to this facility.") boolean used) {

    public FacilityDTO {
        name = name == null ? "" : name;
    }

    public FacilityDTO withId(String id) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }

    public FacilityDTO withName(String name) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }

    public FacilityDTO withLocation(LocationDTO location) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }

    public FacilityDTO withSetupCost(long setupCost) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }

    public FacilityDTO withCapacity(long capacity) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }

    public FacilityDTO withUsedCapacity(long usedCapacity) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }

    public FacilityDTO withUsed(boolean used) {
        return new FacilityDTO(id, name, location, setupCost, capacity, usedCapacity, used);
    }
}
