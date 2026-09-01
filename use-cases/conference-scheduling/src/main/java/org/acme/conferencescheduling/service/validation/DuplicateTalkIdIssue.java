package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class DuplicateTalkIdIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TALK_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate talk code found.");

    @Schema(description = "The code of the duplicated talk.")
    private String talkId;

    public DuplicateTalkIdIssue() {
        this(null);
    }

    public DuplicateTalkIdIssue(String talkId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.talkId = talkId;
    }

    public String getTalkId() {
        return talkId;
    }
}
