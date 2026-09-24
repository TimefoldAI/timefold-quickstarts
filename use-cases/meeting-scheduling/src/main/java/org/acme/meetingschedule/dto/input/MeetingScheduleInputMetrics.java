package org.acme.meetingschedule.dto.input;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the meeting scheduling problem submitted in the input dataset.")
public record MeetingScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_MEETINGS, title = "Meetings",
                format = DataFormat.Values.NUMBER, description = "The number of meetings submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "24", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int meetings,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_PEOPLE, title = "People",
                format = DataFormat.Values.NUMBER, description = "The number of people submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "20", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "20") }) int people,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ROOMS, title = "Rooms",
                format = DataFormat.Values.NUMBER, description = "The number of rooms submitted in the input dataset.",
                type = SchemaType.INTEGER, examples = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int rooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TIME_SLOTS, title = "Time slots",
                format = DataFormat.Values.NUMBER,
                description = "The number of slots a meeting can start in, obtained by dividing the submitted office "
                        + "hours by the submitted granularity.",
                type = SchemaType.INTEGER, examples = "200", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "200") }) int timeSlots)
        implements
            ModelInputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_MEETINGS = "meetings";
    public static final String INPUT_METRIC_PEOPLE = "people";
    public static final String INPUT_METRIC_ROOMS = "rooms";
    public static final String INPUT_METRIC_TIME_SLOTS = "timeSlots";

    public MeetingScheduleInputMetrics {
        if (meetings < 0 || people < 0 || rooms < 0 || timeSlots < 0) {
            throw new IllegalArgumentException(
                    "Input metrics must not be negative, but were meetings (%d), people (%d), rooms (%d), timeSlots (%d)."
                            .formatted(meetings, people, rooms, timeSlots));
        }
    }
}
