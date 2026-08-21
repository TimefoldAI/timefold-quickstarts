package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { BedPlanIssue.class })
public class DuplicateBedIdIssue extends BedPlanIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_BED_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate bed ID found.");

    @Schema(description = "The ID of the duplicated bed.")
    private String bedId;

    public DuplicateBedIdIssue() {
        this(null);
    }

    public DuplicateBedIdIssue(String bedId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.bedId = bedId;
    }

    public String getBedId() {
        return bedId;
    }
}
