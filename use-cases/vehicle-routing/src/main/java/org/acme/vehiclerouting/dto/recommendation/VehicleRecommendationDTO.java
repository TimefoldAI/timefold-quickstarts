package org.acme.vehiclerouting.dto.recommendation;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One way of fitting a visit into the route plan, and what it would cost.
 */
@Schema(description = "A recommended assignment of a visit, with the score difference it would make.")
public record VehicleRecommendationDTO(
        @Schema(description = "The vehicle and position the visit would go to, or null to leave the visit "
                + "unassigned.") VehicleRecommendation proposition,
        @Schema(description = "What applying this recommendation would do to the score.",
                required = true) ScoreAnalysisDTO scoreAnalysisDiff) {
}
