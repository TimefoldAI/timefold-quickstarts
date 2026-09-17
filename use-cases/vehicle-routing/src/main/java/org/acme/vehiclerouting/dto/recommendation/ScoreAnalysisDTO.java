package org.acme.vehiclerouting.dto.recommendation;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.analysis.MatchAnalysis;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;

import org.acme.vehiclerouting.domain.justification.VehicleRoutePlanJustification;
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

    public static ScoreAnalysisDTO of(ScoreAnalysis<HardMediumSoftScore> scoreAnalysis) {
        if (scoreAnalysis == null) {
            return null;
        }
        return new ScoreAnalysisDTO(Objects.toString(scoreAnalysis.score(), null),
                scoreAnalysis.constraintAnalyses().stream()
                        .map(ConstraintAnalysisDTO::of)
                        .toList());
    }

    @Schema(description = "The score difference one constraint accounts for.")
    public record ConstraintAnalysisDTO(
            @Schema(description = "Name of the constraint.", required = true) String name,
            @Schema(description = "Weight of a single match of this constraint.") String weight,
            @Schema(description = "Score difference of every match of this constraint added up.") String score,
            @Schema(description = "How many matches this constraint has.", minimum = "0") int matchCount,
            @Schema(description = "The individual matches, when the analysis was fetched with them.") List<MatchAnalysisDTO> matches) {

        static ConstraintAnalysisDTO of(ConstraintAnalysis<HardMediumSoftScore> constraintAnalysis) {
            // matches() is null when the analysis was fetched shallowly; the match count is always there.
            List<MatchAnalysis<HardMediumSoftScore>> matches = constraintAnalysis.matches();
            return new ConstraintAnalysisDTO(constraintAnalysis.constraintRef().id(),
                    Objects.toString(constraintAnalysis.weight(), null),
                    Objects.toString(constraintAnalysis.score(), null),
                    constraintAnalysis.matchCount(),
                    matches == null ? null : matches.stream().map(MatchAnalysisDTO::of).toList());
        }
    }

    @Schema(description = "One match of a constraint, and why it matched.")
    public record MatchAnalysisDTO(
            @Schema(description = "Score difference of this single match.") String score,
            @Schema(description = "Why the constraint matched.") VehicleRoutePlanJustification justification) {

        static MatchAnalysisDTO of(MatchAnalysis<HardMediumSoftScore> matchAnalysis) {
            // Every constraint of this model justifies with a VehicleRoutePlanJustification; anything
            // else would be a constraint that forgot to, so the match is reported without one rather
            // than failing the whole request.
            var justification = matchAnalysis.justification() instanceof VehicleRoutePlanJustification typed
                    ? typed
                    : null;
            return new MatchAnalysisDTO(Objects.toString(matchAnalysis.score(), null), justification);
        }
    }
}
