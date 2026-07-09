package org.acme.vehiclerouting.dto;

import java.time.OffsetDateTime;
import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The vehicle routing planning problem input.")
public record VehicleRoutingInput(
        @Schema(description = "Display name of the route plan.") String name,
        @Schema(description = "South-west corner of the bounding box of all locations.") LocationDTO southWestCorner,
        @Schema(description = "North-east corner of the bounding box of all locations.") LocationDTO northEastCorner,
        @Schema(description = "Earliest departure time across all vehicles.") OffsetDateTime startDateTime,
        @Schema(description = "Latest end time across all vehicles.") OffsetDateTime endDateTime,
        @Schema(description = "Vehicles available to serve the visits.") List<VehicleDTO> vehicles,
        @Schema(description = "Visits that should be assigned to a vehicle.") List<VisitDTO> visits)
        implements
            ModelInput {

    public VehicleRoutingInput {
        name = name == null ? "" : name;
        vehicles = List.copyOf(vehicles);
        visits = List.copyOf(visits);
    }

    public VehicleRoutingInput withName(String name) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }

    public VehicleRoutingInput withSouthWestCorner(LocationDTO southWestCorner) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }

    public VehicleRoutingInput withNorthEastCorner(LocationDTO northEastCorner) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }

    public VehicleRoutingInput withStartDateTime(OffsetDateTime startDateTime) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }

    public VehicleRoutingInput withEndDateTime(OffsetDateTime endDateTime) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }

    public VehicleRoutingInput withVehicles(List<VehicleDTO> vehicles) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }

    public VehicleRoutingInput withVisits(List<VisitDTO> visits) {
        return new VehicleRoutingInput(name, southWestCorner, northEastCorner, startDateTime, endDateTime, vehicles, visits);
    }
}
