package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.domain.DataFormat;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "Metrics describing the meeting scheduling solution produced for this schedule.")
public record MeetingScheduleOutputMetrics(
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_ASSIGNED_MEETINGS,
                title = "Assigned meetings", format = DataFormat.Values.NUMBER,
                description = "The number of meetings assigned to both a time grain and a room in this schedule.",
                type = SchemaType.INTEGER, example = "24", minimum = "0", readOnly = true,
                extensions = {
                        @Extension(name = X_TF_PRIORITY, value = "1"),
                        @Extension(name = X_TF_EXAMPLE, value = "24") }) int totalAssignedMeetings,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_UNASSIGNED_MEETINGS,
                title = "Unassigned meetings", format = DataFormat.Values.NUMBER,
                description = "The number of meetings left without a time grain or room in this schedule.",
                type = SchemaType.INTEGER, example = "0", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "2"),
                        @Extension(name = X_TF_EXAMPLE, value = "0") }) int totalUnassignedMeetings,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) @Schema(name = TOTAL_USED_ROOMS, title = "Used rooms",
                format = DataFormat.Values.NUMBER,
                description = "The number of distinct rooms used by at least one meeting in this schedule.",
                type = SchemaType.INTEGER, example = "3", minimum = "0", readOnly = true,
                extensions = { @Extension(name = X_TF_PRIORITY, value = "3"),
                        @Extension(name = X_TF_EXAMPLE, value = "3") }) int totalUsedRooms)
        implements
            ModelOutputMetrics{

    private static final String X_TF_PRIORITY = "x-tf-priority";
    private static final String X_TF_EXAMPLE = "x-tf-example";

    public static final String TOTAL_ASSIGNED_MEETINGS = "totalAssignedMeetings";
    public static final String TOTAL_UNASSIGNED_MEETINGS = "totalUnassignedMeetings";
    public static final String TOTAL_USED_ROOMS = "totalUsedRooms";

    public MeetingScheduleOutputMetrics {
        if (totalAssignedMeetings < 0 || totalUnassignedMeetings < 0 || totalUsedRooms < 0) {
            throw new IllegalArgumentException("Output metrics must not be negative.");
        }
    }

    public MeetingScheduleOutputMetrics withTotalAssignedMeetings(int totalAssignedMeetings) {
        return new MeetingScheduleOutputMetrics(totalAssignedMeetings, totalUnassignedMeetings, totalUsedRooms);
    }

    public MeetingScheduleOutputMetrics withTotalUnassignedMeetings(int totalUnassignedMeetings) {
        return new MeetingScheduleOutputMetrics(totalAssignedMeetings, totalUnassignedMeetings, totalUsedRooms);
    }

    public MeetingScheduleOutputMetrics withTotalUsedRooms(int totalUsedRooms) {
        return new MeetingScheduleOutputMetrics(totalAssignedMeetings, totalUnassignedMeetings, totalUsedRooms);
    }
}
