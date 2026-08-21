package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { BedPlanIssue.class })
public class DuplicateDepartmentIdIssue extends BedPlanIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_DEPARTMENT_ID");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate department ID found.");

    @Schema(description = "The ID of the duplicated department.")
    private String departmentId;

    public DuplicateDepartmentIdIssue() {
        this(null);
    }

    public DuplicateDepartmentIdIssue(String departmentId) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        this.departmentId = departmentId;
    }

    public String getDepartmentId() {
        return departmentId;
    }
}
