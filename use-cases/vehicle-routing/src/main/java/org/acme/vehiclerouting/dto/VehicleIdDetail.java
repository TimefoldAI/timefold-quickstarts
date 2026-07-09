package org.acme.vehiclerouting.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a vehicle ID validation issue.")
public record VehicleIdDetail(
        @Schema(description = "The ID of the vehicle.") String vehicleId) implements IssueMetadata {

    public VehicleIdDetail {
        vehicleId = vehicleId == null ? "" : vehicleId;
    }

    public VehicleIdDetail withVehicleId(String vehicleId) {
        return new VehicleIdDetail(vehicleId);
    }

    @Override
    public String getType() {
        return "VehicleId";
    }
}
