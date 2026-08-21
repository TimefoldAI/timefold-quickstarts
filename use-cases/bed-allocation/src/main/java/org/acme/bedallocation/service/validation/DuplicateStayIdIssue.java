package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { BedPlanIssue.class })
public class DuplicateStayIdIssue extends BedPlanIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_STAY_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate stay ID found.");

    @Schema(description = "The ID of the duplicated stay.")
    private String stayId;

    public DuplicateStayIdIssue() {
        this(null);
    }

    public DuplicateStayIdIssue(String stayId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.stayId = stayId;
    }

    public String getStayId() {
        return stayId;
    }
}
