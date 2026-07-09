package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a meeting assignment ID validation issue.")
public record MeetingAssignmentIdDetail(
        @Schema(description = "The ID of the meeting assignment.") String meetingAssignmentId) implements IssueMetadata {

    public MeetingAssignmentIdDetail {
        meetingAssignmentId = meetingAssignmentId == null ? "" : meetingAssignmentId;
    }

    public MeetingAssignmentIdDetail withMeetingAssignmentId(String meetingAssignmentId) {
        return new MeetingAssignmentIdDetail(meetingAssignmentId);
    }

    @Override
    public String getType() {
        return "MeetingAssignmentId";
    }
}
