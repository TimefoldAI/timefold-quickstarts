package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class DuplicateRoomIdIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_ROOM_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate room ID found.");

    @Schema(description = "The ID of the duplicated room.")
    private String roomId;

    public DuplicateRoomIdIssue() {
        this(null);
    }

    public DuplicateRoomIdIssue(String roomId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
