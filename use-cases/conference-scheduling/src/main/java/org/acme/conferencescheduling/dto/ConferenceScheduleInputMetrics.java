package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the conference scheduling problem submitted in the input dataset.")
public record ConferenceScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TALKS, title = "Talks",
                format = DataFormat.Values.NUMBER, description = "The number of talks submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "15", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "15") }) int talks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_SPEAKERS, title = "Speakers",
                format = DataFormat.Values.NUMBER, description = "The number of speakers submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "12", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "12") }) int speakers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ROOMS, title = "Rooms",
                format = DataFormat.Values.NUMBER, description = "The number of rooms submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int rooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TIMESLOTS, title = "Timeslots",
                format = DataFormat.Values.NUMBER, description = "The number of timeslots submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int timeslots,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TALK_TYPES, title = "Talk types",
                format = DataFormat.Values.NUMBER, description = "The number of talk types submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "2", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "2") }) int talkTypes)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_TALKS = "talks";
    public static final String INPUT_METRIC_SPEAKERS = "speakers";
    public static final String INPUT_METRIC_ROOMS = "rooms";
    public static final String INPUT_METRIC_TIMESLOTS = "timeslots";
    public static final String INPUT_METRIC_TALK_TYPES = "talkTypes";

    public ConferenceScheduleInputMetrics {
        if (talks < 0 || speakers < 0 || rooms < 0 || timeslots < 0 || talkTypes < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }
}
