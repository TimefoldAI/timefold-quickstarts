package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a meeting ID validation issue.")
public record MeetingIdDetail(
        @Schema(description = "The ID of the meeting.") String meetingId) implements IssueMetadata {

    public MeetingIdDetail {
        meetingId = meetingId == null ? "" : meetingId;
    }

    public MeetingIdDetail withMeetingId(String meetingId) {
        return new MeetingIdDetail(meetingId);
    }

    @Override
    public String getType() {
        return "MeetingId";
    }
}
