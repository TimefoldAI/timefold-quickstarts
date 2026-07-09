package org.acme.meetingschedule.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A meeting with a topic, a duration and its required and preferred attendees.")
public record MeetingDTO(
        @Schema(description = "Unique identifier of the meeting.") String id,
        @Schema(description = "Topic of the meeting.") String topic,
        @Schema(description = "Duration of the meeting expressed in 15-minute time grains.") int durationInGrains,
        @Schema(description = "IDs of the people required to attend the meeting.") List<String> requiredAttendancePersonIds,
        @Schema(description = "IDs of the people who prefer to attend the meeting.") List<String> preferredAttendancePersonIds) {

    public MeetingDTO {
        topic = topic == null ? "" : topic;
        requiredAttendancePersonIds = List.copyOf(requiredAttendancePersonIds);
        preferredAttendancePersonIds = List.copyOf(preferredAttendancePersonIds);
    }

    public MeetingDTO withId(String id) {
        return new MeetingDTO(id, topic, durationInGrains, requiredAttendancePersonIds, preferredAttendancePersonIds);
    }

    public MeetingDTO withTopic(String topic) {
        return new MeetingDTO(id, topic, durationInGrains, requiredAttendancePersonIds, preferredAttendancePersonIds);
    }

    public MeetingDTO withDurationInGrains(int durationInGrains) {
        return new MeetingDTO(id, topic, durationInGrains, requiredAttendancePersonIds, preferredAttendancePersonIds);
    }

    public MeetingDTO withRequiredAttendancePersonIds(List<String> requiredAttendancePersonIds) {
        return new MeetingDTO(id, topic, durationInGrains, requiredAttendancePersonIds, preferredAttendancePersonIds);
    }

    public MeetingDTO withPreferredAttendancePersonIds(List<String> preferredAttendancePersonIds) {
        return new MeetingDTO(id, topic, durationInGrains, requiredAttendancePersonIds, preferredAttendancePersonIds);
    }
}
