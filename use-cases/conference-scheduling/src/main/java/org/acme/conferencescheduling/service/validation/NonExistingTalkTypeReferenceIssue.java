package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class NonExistingTalkTypeReferenceIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TALK_TYPE_REFERENCE");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Talk references non-existing talk type.");

    @Schema(description = "The code of the talk with the dangling reference, if it has one.")
    private String talkId;

    public NonExistingTalkTypeReferenceIssue() {
        this(null);
    }

    public NonExistingTalkTypeReferenceIssue(String talkId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.talkId = talkId;
    }

    public String getTalkId() {
        return talkId;
    }
}
