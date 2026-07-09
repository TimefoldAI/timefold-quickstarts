package org.acme.tournamentschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the tournament scheduling solution produced for this schedule.")
public record TournamentScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_TEAM_ASSIGNMENTS,
                title = "Assigned team assignments", format = DataFormat.Values.NUMBER,
                description = "The number of slots assigned to a team in this schedule.",
                type = SchemaType.INTEGER, example = "72", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "72") }) int totalAssignedTeamAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_TEAM_ASSIGNMENTS,
                title = "Unassigned team assignments", format = DataFormat.Values.NUMBER,
                description = "The number of slots left without a team in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedTeamAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_TEAMS, title = "Used teams",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct teams used by at least one assignment in this schedule.",
                type = SchemaType.INTEGER, example = "7", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "7") }) int totalUsedTeams,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_DAYS, title = "Used days",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct days used by at least one assignment in this schedule.",
                type = SchemaType.INTEGER, example = "18", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "18") }) int totalUsedDays)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_TEAM_ASSIGNMENTS = "totalAssignedTeamAssignments";
    public static final String TOTAL_UNASSIGNED_TEAM_ASSIGNMENTS = "totalUnassignedTeamAssignments";
    public static final String TOTAL_USED_TEAMS = "totalUsedTeams";
    public static final String TOTAL_USED_DAYS = "totalUsedDays";

    public TournamentScheduleOutputMetrics {
        if (totalAssignedTeamAssignments < 0 || totalUnassignedTeamAssignments < 0 || totalUsedTeams < 0
                || totalUsedDays < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public TournamentScheduleOutputMetrics withTotalAssignedTeamAssignments(int totalAssignedTeamAssignments) {
        return new TournamentScheduleOutputMetrics(totalAssignedTeamAssignments, totalUnassignedTeamAssignments,
                totalUsedTeams, totalUsedDays);
    }

    public TournamentScheduleOutputMetrics withTotalUnassignedTeamAssignments(int totalUnassignedTeamAssignments) {
        return new TournamentScheduleOutputMetrics(totalAssignedTeamAssignments, totalUnassignedTeamAssignments,
                totalUsedTeams, totalUsedDays);
    }

    public TournamentScheduleOutputMetrics withTotalUsedTeams(int totalUsedTeams) {
        return new TournamentScheduleOutputMetrics(totalAssignedTeamAssignments, totalUnassignedTeamAssignments,
                totalUsedTeams, totalUsedDays);
    }

    public TournamentScheduleOutputMetrics withTotalUsedDays(int totalUsedDays) {
        return new TournamentScheduleOutputMetrics(totalAssignedTeamAssignments, totalUnassignedTeamAssignments,
                totalUsedTeams, totalUsedDays);
    }
}
