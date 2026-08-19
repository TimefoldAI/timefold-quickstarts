package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class TalkIdMissingIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("TALK_ID_MISSING");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Talk code must not be null or blank.");

    public TalkIdMissingIssue() {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
    }
}
