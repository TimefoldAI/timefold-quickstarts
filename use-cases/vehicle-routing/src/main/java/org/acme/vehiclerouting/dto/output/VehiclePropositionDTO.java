package org.acme.vehiclerouting.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Where a visit would go if a recommendation were applied: the vehicle that would service it, and
 * the position it would take in that vehicle's route.
 */
@Schema(description = "A place on a vehicle's route that a visit could be inserted at.")
public record VehiclePropositionDTO(
        @Schema(description = "Unique identifier of the vehicle that would service the visit.",
                required = true) String vehicleId,
        @Schema(description = "Zero-based position the visit would take in that vehicle's route.",
                required = true, minimum = "0") int index) {
}
