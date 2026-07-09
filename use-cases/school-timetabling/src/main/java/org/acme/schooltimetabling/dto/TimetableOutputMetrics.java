package org.acme.schooltimetabling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the school timetabling solution produced for this schedule.")
public record TimetableOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_SCHEDULED_LESSONS,
                title = "Scheduled lessons", format = DataFormat.Values.NUMBER,
                description = "The number of lessons assigned to both a timeslot and a room in this schedule.",
                type = SchemaType.INTEGER, example = "30", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "30") }) int totalScheduledLessons,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNSCHEDULED_LESSONS,
                title = "Unscheduled lessons", format = DataFormat.Values.NUMBER,
                description = "The number of lessons left without a timeslot or room in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnscheduledLessons,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROOMS, title = "Used rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rooms used by at least one lesson in this schedule.",
                type = SchemaType.INTEGER, example = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int totalUsedRooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_TIMESLOTS, title = "Used timeslots",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct timeslots used by at least one lesson in this schedule.",
                type = SchemaType.INTEGER, example = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int totalUsedTimeslots)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_SCHEDULED_LESSONS = "totalScheduledLessons";
    public static final String TOTAL_UNSCHEDULED_LESSONS = "totalUnscheduledLessons";
    public static final String TOTAL_USED_ROOMS = "totalUsedRooms";
    public static final String TOTAL_USED_TIMESLOTS = "totalUsedTimeslots";

    public TimetableOutputMetrics {
        if (totalScheduledLessons < 0 || totalUnscheduledLessons < 0 || totalUsedRooms < 0 || totalUsedTimeslots < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public TimetableOutputMetrics withTotalScheduledLessons(int totalScheduledLessons) {
        return new TimetableOutputMetrics(totalScheduledLessons, totalUnscheduledLessons, totalUsedRooms,
                totalUsedTimeslots);
    }

    public TimetableOutputMetrics withTotalUnscheduledLessons(int totalUnscheduledLessons) {
        return new TimetableOutputMetrics(totalScheduledLessons, totalUnscheduledLessons, totalUsedRooms,
                totalUsedTimeslots);
    }

    public TimetableOutputMetrics withTotalUsedRooms(int totalUsedRooms) {
        return new TimetableOutputMetrics(totalScheduledLessons, totalUnscheduledLessons, totalUsedRooms,
                totalUsedTimeslots);
    }

    public TimetableOutputMetrics withTotalUsedTimeslots(int totalUsedTimeslots) {
        return new TimetableOutputMetrics(totalScheduledLessons, totalUnscheduledLessons, totalUsedRooms,
                totalUsedTimeslots);
    }
}
