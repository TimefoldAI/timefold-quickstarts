package org.acme.vehiclerouting.dto.output;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * What one recommendation would do to the score, constraint by constraint.
 * <p>
 * This mirrors the shape of the Service module's own score analysis, so the same rendering applies
 * to both: a list of constraints, each with the matches behind it and the justification that
 * explains every match.
 */
@Schema(description = "The score difference one recommended assignment would make, per constraint.")
public record ScoreAnalysisDTO(
        @Schema(description = "The score difference as a whole, in the solver's score notation.") String score,
        @Schema(description = "One entry per constraint whose score the recommendation changes.",
                required = true) List<ConstraintAnalysisDTO> constraints) {
}
