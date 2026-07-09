package org.acme.schooltimetabling.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the school timetabling problem submitted in the input dataset.")
public record TimetableInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_LESSONS, title = "Lessons",
                format = DataFormat.Values.NUMBER,
                description = "The number of lessons submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "30", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "30") }) int lessons,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TIMESLOTS, title = "Timeslots",
                format = DataFormat.Values.NUMBER,
                description = "The number of timeslots submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "10", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "10") }) int timeslots,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ROOMS, title = "Rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of rooms submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int rooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TEACHERS, title = "Teachers",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct teachers submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "6", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "6") }) int teachers,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_STUDENT_GROUPS,
                title = "Student groups", format = DataFormat.Values.NUMBER,
                description = "The number of distinct student groups submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "2", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "2") }) int studentGroups)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_LESSONS = "lessons";
    public static final String INPUT_METRIC_TIMESLOTS = "timeslots";
    public static final String INPUT_METRIC_ROOMS = "rooms";
    public static final String INPUT_METRIC_TEACHERS = "teachers";
    public static final String INPUT_METRIC_STUDENT_GROUPS = "studentGroups";

    public TimetableInputMetrics {
        if (lessons < 0 || timeslots < 0 || rooms < 0 || teachers < 0 || studentGroups < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public TimetableInputMetrics withLessons(int lessons) {
        return new TimetableInputMetrics(lessons, timeslots, rooms, teachers, studentGroups);
    }

    public TimetableInputMetrics withTimeslots(int timeslots) {
        return new TimetableInputMetrics(lessons, timeslots, rooms, teachers, studentGroups);
    }

    public TimetableInputMetrics withRooms(int rooms) {
        return new TimetableInputMetrics(lessons, timeslots, rooms, teachers, studentGroups);
    }

    public TimetableInputMetrics withTeachers(int teachers) {
        return new TimetableInputMetrics(lessons, timeslots, rooms, teachers, studentGroups);
    }

    public TimetableInputMetrics withStudentGroups(int studentGroups) {
        return new TimetableInputMetrics(lessons, timeslots, rooms, teachers, studentGroups);
    }
}
