package org.acme.vehiclerouting.dto.recommendation;

import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Asks where one visit would best fit into a route plan. The visit has to be part of
 * {@code modelInput} and has to be unassigned there, that is, appear in no vehicle's
 * {@code visitIds}.
 */
@Schema(description = "A request for recommended assignments of a single, still unassigned visit.")
public record RecommendationRequest(
        @Schema(description = "The route plan to fit the visit into. Every other visit's assignment is taken as is.",
                required = true) VehicleRoutePlanInput modelInput,
        @Schema(description = "Id of the visit to find a place for. Must be one of the plan's visits and must not be "
                + "on any vehicle's route yet.", required = true, minLength = 1) String visitId) {
}
