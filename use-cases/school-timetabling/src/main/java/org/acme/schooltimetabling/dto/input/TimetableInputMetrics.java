package org.acme.schooltimetabling.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics describing the school timetabling problem submitted in the input dataset.")
public record TimetableInputMetrics(
        @Schema(title = "Lessons", format = DataFormat.Values.NUMBER,
                description = "The number of lessons submitted in the input dataset.",
                examples = "20", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "20") }) int lessons,
        @Schema(title = "Timeslots", format = DataFormat.Values.NUMBER,
                description = "The number of timeslots submitted in the input dataset.",
                examples = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int timeslots,
        @Schema(title = "Rooms", format = DataFormat.Values.NUMBER,
                description = "The number of rooms submitted in the input dataset.",
                examples = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int rooms,
        @Schema(title = "Teachers", format = DataFormat.Values.NUMBER,
                description = "The number of distinct teachers giving the lessons.",
                examples = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int teachers,
        @Schema(title = "Student groups", format = DataFormat.Values.NUMBER,
                description = "The number of distinct student groups attending the lessons.",
                examples = "2", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "2") }) int studentGroups)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public TimetableInputMetrics {
        if (lessons < 0 || timeslots < 0 || rooms < 0 || teachers < 0 || studentGroups < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were lessons (%d), timeslots (%d), rooms (%d), teachers (%d), studentGroups (%d)."
                            .formatted(lessons, timeslots, rooms, teachers, studentGroups));
        }
    }
}
