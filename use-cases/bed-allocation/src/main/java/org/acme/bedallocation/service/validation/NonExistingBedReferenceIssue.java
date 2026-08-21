package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { BedPlanIssue.class })
public class NonExistingBedReferenceIssue extends BedPlanIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_BED_REFERENCE");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Stay refers to a bed ID that does not exist.");

    @Schema(description = "The ID of the stay with the unknown bed reference.")
    private String stayId;

    public NonExistingBedReferenceIssue() {
        this(null);
    }

    public NonExistingBedReferenceIssue(String stayId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.stayId = stayId;
    }

    public String getStayId() {
        return stayId;
    }
}
