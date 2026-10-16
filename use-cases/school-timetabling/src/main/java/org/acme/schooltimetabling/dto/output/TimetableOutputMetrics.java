package org.acme.schooltimetabling.dto.output;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the timetable produced for this dataset.")
public record TimetableOutputMetrics(
        @Schema(title = "Scheduled lessons", format = DataFormat.Values.NUMBER,
                description = "The number of lessons that got both a timeslot and a room in this timetable.",
                examples = "20", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "20") }) int totalAssignedLessons,
        @Schema(title = "Unscheduled lessons", format = DataFormat.Values.NUMBER,
                description = "The number of lessons left without a timeslot or a room in this timetable.",
                examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedLessons)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public TimetableOutputMetrics {
        if (totalAssignedLessons < 0 || totalUnassignedLessons < 0) {
            throw new IllegalArgumentException(
                    "Output metrics must not be negative, but were totalAssignedLessons (%d), totalUnassignedLessons (%d)."
                            .formatted(totalAssignedLessons, totalUnassignedLessons));
        }
    }
}
