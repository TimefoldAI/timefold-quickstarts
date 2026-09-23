package org.acme.tournamentschedule.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the tournament schedule produced for this dataset.")
public record TournamentScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_MATCHES,
                title = "Assigned matches", format = DataFormat.Values.NUMBER,
                description = "The number of match slots assigned a team in this schedule.",
                type = SchemaType.INTEGER, examples = "72", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "72") }) int totalAssignedMatches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_MATCHES,
                title = "Unassigned matches", format = DataFormat.Values.NUMBER,
                description = "The number of match slots left without a team in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedMatches,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = ASSIGNMENT_COUNT_RANGE,
                title = "Assignment count range", format = DataFormat.Values.NUMBER,
                description = "The difference between the most and the fewest matches any single team is assigned "
                        + "in this schedule; 0 means every team has exactly the same number of matches.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int assignmentCountRange)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_MATCHES = "totalAssignedMatches";
    public static final String TOTAL_UNASSIGNED_MATCHES = "totalUnassignedMatches";
    public static final String ASSIGNMENT_COUNT_RANGE = "assignmentCountRange";

    public TournamentScheduleOutputMetrics {
        if (totalAssignedMatches < 0 || totalUnassignedMatches < 0 || assignmentCountRange < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedMatches (%d), totalUnassignedMatches (%d), assignmentCountRange (%d)."
                            .formatted(totalAssignedMatches, totalUnassignedMatches, assignmentCountRange));
        }
    }
}
