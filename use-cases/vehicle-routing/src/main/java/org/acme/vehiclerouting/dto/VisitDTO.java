package org.acme.vehiclerouting.dto;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A visit that must be served by a vehicle within its time window.")
public record VisitDTO(
        @Schema(description = "Unique identifier of the visit.") String id,
        @Schema(description = "Display name of the visit.") String name,
        @Schema(description = "Geographic location of the visit.") LocationDTO location,
        @Schema(description = "Demand of the visit consumed from the vehicle capacity.") int demand,
        @Schema(description = "Earliest time the service may start.") OffsetDateTime minStartTime,
        @Schema(description = "Latest time the service must be finished by.") OffsetDateTime maxEndTime,
        @Schema(description = "Service duration in seconds.") long serviceDurationSeconds,
        @Schema(description = "Identifier of the assigned vehicle, or null when unassigned.") String vehicleId,
        @Schema(description = "Computed arrival time, or null when unassigned.") OffsetDateTime arrivalTime,
        @Schema(description = "Computed service start time, or null when unassigned.") OffsetDateTime startServiceTime,
        @Schema(description = "Computed departure time, or null when unassigned.") OffsetDateTime departureTime,
        @Schema(
                description = "Driving time in seconds from the previous standstill.") long drivingTimeSecondsFromPreviousStandstill) {

    public VisitDTO {
        id = id == null ? "" : id;
        name = name == null ? "" : name;
    }

    public VisitDTO withId(String id) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withName(String name) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withLocation(LocationDTO location) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withDemand(int demand) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withMinStartTime(OffsetDateTime minStartTime) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withMaxEndTime(OffsetDateTime maxEndTime) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withServiceDurationSeconds(long serviceDurationSeconds) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withVehicleId(String vehicleId) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withArrivalTime(OffsetDateTime arrivalTime) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withStartServiceTime(OffsetDateTime startServiceTime) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withDepartureTime(OffsetDateTime departureTime) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }

    public VisitDTO withDrivingTimeSecondsFromPreviousStandstill(long drivingTimeSecondsFromPreviousStandstill) {
        return new VisitDTO(id, name, location, demand, minStartTime, maxEndTime, serviceDurationSeconds, vehicleId,
                arrivalTime, startServiceTime, departureTime, drivingTimeSecondsFromPreviousStandstill);
    }
}
