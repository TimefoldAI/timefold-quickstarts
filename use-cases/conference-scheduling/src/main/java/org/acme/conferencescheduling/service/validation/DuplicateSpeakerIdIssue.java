package org.acme.conferencescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { ConferenceScheduleIssue.class })
public class DuplicateSpeakerIdIssue extends ConferenceScheduleIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_SPEAKER_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate speaker ID found.");

    @Schema(description = "The ID of the duplicated speaker.")
    private String speakerId;

    public DuplicateSpeakerIdIssue() {
        this(null);
    }

    public DuplicateSpeakerIdIssue(String speakerId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.speakerId = speakerId;
    }

    public String getSpeakerId() {
        return speakerId;
    }
}
