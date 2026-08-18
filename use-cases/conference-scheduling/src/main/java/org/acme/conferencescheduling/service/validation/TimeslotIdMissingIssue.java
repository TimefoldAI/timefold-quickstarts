package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class TimeslotIdMissingIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("TIMESLOT_ID_MISSING");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Timeslot ID must not be null or blank.");

    public TimeslotIdMissingIssue() {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
    }
}
