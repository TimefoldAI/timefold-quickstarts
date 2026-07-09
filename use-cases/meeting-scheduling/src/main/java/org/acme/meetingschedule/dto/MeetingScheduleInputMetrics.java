package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the meeting scheduling problem submitted in the input dataset.")
public record MeetingScheduleInputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_MEETINGS, title = "Meetings",
                format = DataFormat.Values.NUMBER,
                description = "The number of meetings submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int meetings,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_MEETING_ASSIGNMENTS,
                title = "Meeting assignments", format = DataFormat.Values.NUMBER,
                description = "The number of meeting assignments submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int meetingAssignments,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_PEOPLE, title = "People",
                format = DataFormat.Values.NUMBER,
                description = "The number of people submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "20", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "20") }) int people,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_ROOMS, title = "Rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of rooms submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "4"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int rooms,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = INPUT_METRIC_TIME_GRAINS, title = "Time grains",
                format = DataFormat.Values.NUMBER,
                description = "The number of time grains submitted in the input dataset.",
                type = SchemaType.INTEGER, example = "160", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "5"),
                        @Extension(name = X_TF_EXAMPLE, value = "160") }) int timeGrains)
        implements
            ModelInputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String INPUT_METRIC_MEETINGS = "meetings";
    public static final String INPUT_METRIC_MEETING_ASSIGNMENTS = "meetingAssignments";
    public static final String INPUT_METRIC_PEOPLE = "people";
    public static final String INPUT_METRIC_ROOMS = "rooms";
    public static final String INPUT_METRIC_TIME_GRAINS = "timeGrains";

    public MeetingScheduleInputMetrics {
        if (meetings < 0 || meetingAssignments < 0 || people < 0 || rooms < 0 || timeGrains < 0) {
            throw new IllegalArgumentException("Input metrics must not be negative.");
        }
    }

    public MeetingScheduleInputMetrics withMeetings(int meetings) {
        return new MeetingScheduleInputMetrics(meetings, meetingAssignments, people, rooms, timeGrains);
    }

    public MeetingScheduleInputMetrics withMeetingAssignments(int meetingAssignments) {
        return new MeetingScheduleInputMetrics(meetings, meetingAssignments, people, rooms, timeGrains);
    }

    public MeetingScheduleInputMetrics withPeople(int people) {
        return new MeetingScheduleInputMetrics(meetings, meetingAssignments, people, rooms, timeGrains);
    }

    public MeetingScheduleInputMetrics withRooms(int rooms) {
        return new MeetingScheduleInputMetrics(meetings, meetingAssignments, people, rooms, timeGrains);
    }

    public MeetingScheduleInputMetrics withTimeGrains(int timeGrains) {
        return new MeetingScheduleInputMetrics(meetings, meetingAssignments, people, rooms, timeGrains);
    }
}
