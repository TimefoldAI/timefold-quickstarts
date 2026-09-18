package org.acme.vehiclerouting.dto.output;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * The part of a {@link ScoreAnalysisDTO} that one constraint accounts for.
 */
@Schema(description = "The score difference one constraint accounts for.")
public record ConstraintAnalysisDTO(
        @Schema(description = "Name of the constraint.", required = true) String name,
        @Schema(description = "Weight of a single match of this constraint.") String weight,
        @Schema(description = "Score difference of every match of this constraint added up.") String score,
        @Schema(description = "How many matches this constraint has.", minimum = "0") int matchCount,
        @Schema(description = "The individual matches, when the analysis was fetched with them.") List<MatchAnalysisDTO> matches) {
}
