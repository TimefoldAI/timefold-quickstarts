package org.acme.vehiclerouting.dto;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A vehicle with a capacity and a home location that is assigned an ordered list of visits.")
public record VehicleDTO(
        @Schema(description = "Unique identifier of the vehicle.") String id,
        @Schema(description = "Maximum total demand the vehicle can carry.") int capacity,
        @Schema(description = "Home location the vehicle departs from and returns to.") LocationDTO homeLocation,
        @Schema(description = "ISO-8601 time at which the vehicle departs from its home location.") OffsetDateTime departureTime,
        @Schema(description = "Ordered list of visit IDs assigned to this vehicle.") List<String> visitIds,
        @Schema(description = "Total demand of all assigned visits.") int totalDemand,
        @Schema(description = "Total driving time in seconds of the vehicle route.") long totalDrivingTimeSeconds,
        @Schema(description = "ISO-8601 time at which the vehicle arrives back home, or null when unassigned.") OffsetDateTime arrivalTime) {

    public VehicleDTO {
        id = id == null ? "" : id;
        visitIds = List.copyOf(visitIds);
    }

    public VehicleDTO withId(String id) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withCapacity(int capacity) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withHomeLocation(LocationDTO homeLocation) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withDepartureTime(OffsetDateTime departureTime) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withVisitIds(List<String> visitIds) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withTotalDemand(int totalDemand) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withTotalDrivingTimeSeconds(long totalDrivingTimeSeconds) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }

    public VehicleDTO withArrivalTime(OffsetDateTime arrivalTime) {
        return new VehicleDTO(id, capacity, homeLocation, departureTime, visitIds, totalDemand, totalDrivingTimeSeconds,
                arrivalTime);
    }
}
