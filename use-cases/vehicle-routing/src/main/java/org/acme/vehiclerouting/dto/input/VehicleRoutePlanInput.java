package org.acme.vehiclerouting.dto.input;

import java.time.OffsetDateTime;
import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The vehicle routing problem input.")
public record VehicleRoutePlanInput(
        @Schema(description = "Start of the planning window, in ISO-8601 date-time format with an offset.",
                required = true) OffsetDateTime startDateTime,
        @Schema(description = "End of the planning window, in ISO-8601 date-time format with an offset.",
                required = true) OffsetDateTime endDateTime,
        @Schema(description = "Vehicles that visits can be assigned to.", required = true,
                minItems = 1) List<VehicleInputDTO> vehicles,
        @Schema(description = "Visits that should each be serviced by one of the vehicles.", required = true,
                minItems = 1) List<VisitInputDTO> visits)
        implements
            ModelInput {

    public VehicleRoutePlanInput withVehicles(List<VehicleInputDTO> vehicles) {
        return new VehicleRoutePlanInput(startDateTime, endDateTime, vehicles, visits);
    }
}
