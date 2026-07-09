package org.acme.sportsleagueschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the sports league scheduling solution produced for this schedule.")
public record LeagueScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_MATCHES, title = "Assigned matches",
                format = DataFormat.Values.NUMBER,
                description = "The number of matches assigned to a round in this schedule.",
                type = SchemaType.INTEGER, example = "182", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "182") }) int totalAssignedMatches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_MATCHES,
                title = "Unassigned matches", format = DataFormat.Values.NUMBER,
                description = "The number of matches left without a round in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedMatches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROUNDS, title = "Used rounds",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rounds used by at least one match in this schedule.",
                type = SchemaType.INTEGER, example = "32", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "32") }) int totalUsedRounds)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_MATCHES = "totalAssignedMatches";
    public static final String TOTAL_UNASSIGNED_MATCHES = "totalUnassignedMatches";
    public static final String TOTAL_USED_ROUNDS = "totalUsedRounds";

    public LeagueScheduleOutputMetrics {
        if (totalAssignedMatches < 0 || totalUnassignedMatches < 0 || totalUsedRounds < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public LeagueScheduleOutputMetrics withTotalAssignedMatches(int totalAssignedMatches) {
        return new LeagueScheduleOutputMetrics(totalAssignedMatches, totalUnassignedMatches, totalUsedRounds);
    }

    public LeagueScheduleOutputMetrics withTotalUnassignedMatches(int totalUnassignedMatches) {
        return new LeagueScheduleOutputMetrics(totalAssignedMatches, totalUnassignedMatches, totalUsedRounds);
    }

    public LeagueScheduleOutputMetrics withTotalUsedRounds(int totalUsedRounds) {
        return new LeagueScheduleOutputMetrics(totalAssignedMatches, totalUnassignedMatches, totalUsedRounds);
    }
}
