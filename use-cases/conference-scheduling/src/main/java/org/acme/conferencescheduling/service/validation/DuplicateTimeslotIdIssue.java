package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class DuplicateTimeslotIdIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TIMESLOT_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate timeslot ID found.");

    @Schema(description = "The ID of the duplicated timeslot.")
    private String timeslotId;

    public DuplicateTimeslotIdIssue() {
        this(null);
    }

    public DuplicateTimeslotIdIssue(String timeslotId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.timeslotId = timeslotId;
    }

    public String getTimeslotId() {
        return timeslotId;
    }
}
