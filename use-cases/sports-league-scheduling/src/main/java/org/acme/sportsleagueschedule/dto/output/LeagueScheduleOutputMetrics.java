package org.acme.sportsleagueschedule.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the league schedule produced for this dataset.")
public record LeagueScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_MATCHES,
                title = "Assigned matches", format = DataFormat.Values.NUMBER,
                description = "The number of matches assigned to a round in this schedule.",
                type = SchemaType.INTEGER, examples = "182", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "182") }) int totalAssignedMatches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_MATCHES,
                title = "Unassigned matches", format = DataFormat.Values.NUMBER,
                description = "The number of matches left without a round in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedMatches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROUNDS, title = "Used rounds",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rounds holding at least one match in this schedule.",
                type = SchemaType.INTEGER, examples = "32", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "32") }) int totalUsedRounds,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_CLASSIC_MATCHES_OFF_PEAK,
                title = "Classic matches off peak", format = DataFormat.Values.NUMBER,
                description = "The number of classic matches played on a round that is neither a weekend nor a holiday.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalClassicMatchesOffPeak)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_MATCHES = "totalAssignedMatches";
    public static final String TOTAL_UNASSIGNED_MATCHES = "totalUnassignedMatches";
    public static final String TOTAL_USED_ROUNDS = "totalUsedRounds";
    public static final String TOTAL_CLASSIC_MATCHES_OFF_PEAK = "totalClassicMatchesOffPeak";

    public LeagueScheduleOutputMetrics {
        if (totalAssignedMatches < 0 || totalUnassignedMatches < 0 || totalUsedRounds < 0
                || totalClassicMatchesOffPeak < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedMatches (%d), totalUnassignedMatches (%d), totalUsedRounds (%d), totalClassicMatchesOffPeak (%d)."
                            .formatted(totalAssignedMatches, totalUnassignedMatches, totalUsedRounds,
                                    totalClassicMatchesOffPeak));
        }
    }
}
