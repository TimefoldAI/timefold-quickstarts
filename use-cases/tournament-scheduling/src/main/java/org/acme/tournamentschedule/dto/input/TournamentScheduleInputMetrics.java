package org.acme.tournamentschedule.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the tournament scheduling problem submitted in the input dataset.")
public record TournamentScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TEAMS, title = "Teams",
                format = DataFormat.Values.NUMBER, description = "The number of teams submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "7", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "7") }) int teams,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_MATCHES, title = "Matches",
                format = DataFormat.Values.NUMBER,
                description = "The number of match slots submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "72", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "72") }) int matches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_UNAVAILABILITIES,
                title = "Unavailabilities", format = DataFormat.Values.NUMBER,
                description = "The number of team unavailability days submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "12", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "12") }) int unavailabilities)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_TEAMS = "teams";
    public static final String INPUT_METRIC_MATCHES = "matches";
    public static final String INPUT_METRIC_UNAVAILABILITIES = "unavailabilities";

    public TournamentScheduleInputMetrics {
        if (teams < 0 || matches < 0 || unavailabilities < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were teams (%d), matches (%d), unavailabilities (%d)."
                            .formatted(teams, matches, unavailabilities));
        }
    }
}
