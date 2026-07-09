package org.acme.tournamentschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the tournament scheduling problem submitted in the input dataset.")
public record TournamentScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TEAMS, title = "Teams",
                format = DataFormat.Values.NUMBER,
                description = "The number of teams submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "7", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "7") }) int teams,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_DAYS, title = "Days",
                format = DataFormat.Values.NUMBER,
                description = "The number of days submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "18", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "18") }) int days,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TEAM_ASSIGNMENTS,
                title = "Team assignments", format = DataFormat.Values.NUMBER,
                description = "The number of team assignment slots submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "72", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "72") }) int teamAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_UNAVAILABILITY_PENALTIES,
                title = "Unavailability penalties", format = DataFormat.Values.NUMBER,
                description = "The number of unavailability penalties submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "12", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "12") }) int unavailabilityPenalties)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_TEAMS = "teams";
    public static final String INPUT_METRIC_DAYS = "days";
    public static final String INPUT_METRIC_TEAM_ASSIGNMENTS = "teamAssignments";
    public static final String INPUT_METRIC_UNAVAILABILITY_PENALTIES = "unavailabilityPenalties";

    public TournamentScheduleInputMetrics {
        if (teams < 0 || days < 0 || teamAssignments < 0 || unavailabilityPenalties < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public TournamentScheduleInputMetrics withTeams(int teams) {
        return new TournamentScheduleInputMetrics(teams, days, teamAssignments, unavailabilityPenalties);
    }

    public TournamentScheduleInputMetrics withDays(int days) {
        return new TournamentScheduleInputMetrics(teams, days, teamAssignments, unavailabilityPenalties);
    }

    public TournamentScheduleInputMetrics withTeamAssignments(int teamAssignments) {
        return new TournamentScheduleInputMetrics(teams, days, teamAssignments, unavailabilityPenalties);
    }

    public TournamentScheduleInputMetrics withUnavailabilityPenalties(int unavailabilityPenalties) {
        return new TournamentScheduleInputMetrics(teams, days, teamAssignments, unavailabilityPenalties);
    }
}
