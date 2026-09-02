package org.acme.vehiclerouting.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The vehicle routing problem output.")
public record VehicleRoutePlanOutput(
        @Schema(description = "Vehicles with the route each of them drives.", required = true) List<VehicleOutputDTO> vehicles,
        @Schema(description = "Visits with the vehicle and the times they are serviced at, if any.",
                required = true) List<VisitOutputDTO> visits)
        implements
            ModelOutput {
}
