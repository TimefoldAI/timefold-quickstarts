package org.acme.facilitylocation.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A consumer with a demand that must be assigned to a facility with sufficient capacity.")
public record ConsumerDTO(
        @Schema(description = "Unique identifier of the consumer.") String id,
        @Schema(description = "Geographic location of the consumer.") LocationDTO location,
        @Schema(description = "Capacity demand of this consumer that the assigned facility must satisfy.") long demand,
        @Schema(description = "The ID of the facility assigned to serve this consumer. Null when unassigned.") String facilityId,
        @Schema(description = "True if this consumer is assigned to a facility.") boolean assigned) {

    public ConsumerDTO {
        facilityId = normalizeFacilityId(facilityId);
    }

    private static String normalizeFacilityId(String facilityId) {
        return facilityId != null && facilityId.isBlank() ? null : facilityId;
    }

    public ConsumerDTO withId(String id) {
        return new ConsumerDTO(id, location, demand, facilityId, assigned);
    }

    public ConsumerDTO withLocation(LocationDTO location) {
        return new ConsumerDTO(id, location, demand, facilityId, assigned);
    }

    public ConsumerDTO withDemand(long demand) {
        return new ConsumerDTO(id, location, demand, facilityId, assigned);
    }

    public ConsumerDTO withFacilityId(String facilityId) {
        return new ConsumerDTO(id, location, demand, facilityId, assigned);
    }

    public ConsumerDTO withAssigned(boolean assigned) {
        return new ConsumerDTO(id, location, demand, facilityId, assigned);
    }
}
