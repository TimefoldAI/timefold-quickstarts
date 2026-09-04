package org.acme.sportsleagueschedule.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the sports league scheduling problem submitted in the input dataset.")
public record LeagueScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_MATCHES, title = "Matches",
                format = DataFormat.Values.NUMBER,
                description = "The number of matches submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "182", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "182") }) int matches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ROUNDS, title = "Rounds",
                format = DataFormat.Values.NUMBER,
                description = "The number of matchdays the matches can be played on.",
                type = SchemaType.INTEGER, examples = "32", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "32") }) int rounds,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TEAMS, title = "Teams",
                format = DataFormat.Values.NUMBER,
                description = "The number of teams competing in the league.",
                type = SchemaType.INTEGER, examples = "14", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "14") }) int teams)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_MATCHES = "matches";
    public static final String INPUT_METRIC_ROUNDS = "rounds";
    public static final String INPUT_METRIC_TEAMS = "teams";

    public LeagueScheduleInputMetrics {
        if (matches < 0 || rounds < 0 || teams < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were matches (%d), rounds (%d), teams (%d)."
                            .formatted(matches, rounds, teams));
        }
    }
}
