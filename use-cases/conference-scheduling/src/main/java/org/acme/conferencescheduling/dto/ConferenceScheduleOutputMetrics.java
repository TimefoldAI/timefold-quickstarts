package org.acme.conferencescheduling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the conference scheduling solution produced for this schedule.")
public record ConferenceScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_SCHEDULED_TALKS, title = "Scheduled talks",
                format = DataFormat.Values.NUMBER,
                description = "The number of talks assigned to both a timeslot and a room in this schedule.",
                type = SchemaType.INTEGER, examples = "15", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "15") }) int totalScheduledTalks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNSCHEDULED_TALKS, title = "Unscheduled talks",
                format = DataFormat.Values.NUMBER,
                description = "The number of talks left without a timeslot or room in this schedule.",
                type = SchemaType.INTEGER, examples = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnscheduledTalks,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROOMS, title = "Used rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rooms used by at least one talk in this schedule.",
                type = SchemaType.INTEGER, examples = "5", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "5") }) int totalUsedRooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_TIMESLOTS, title = "Used timeslots",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct timeslots used by at least one talk in this schedule.",
                type = SchemaType.INTEGER, examples = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int totalUsedTimeslots)
        implements
            ModelOutputMetrics {

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_SCHEDULED_TALKS = "totalScheduledTalks";
    public static final String TOTAL_UNSCHEDULED_TALKS = "totalUnscheduledTalks";
    public static final String TOTAL_USED_ROOMS = "totalUsedRooms";
    public static final String TOTAL_USED_TIMESLOTS = "totalUsedTimeslots";

    public ConferenceScheduleOutputMetrics {
        if (totalScheduledTalks < 0 || totalUnscheduledTalks < 0 || totalUsedRooms < 0 || totalUsedTimeslots < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }
}
