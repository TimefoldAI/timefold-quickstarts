package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { BedPlanIssue.class })
public class OpenApiSpecIssue extends BedPlanIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("OPEN_API_SPEC_ISSUE");
    public OpenApiSpecIssue() {
        this("Input conflicts with OpenAPI specification.");
    }

    public OpenApiSpecIssue(String message) {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(new IssueMessage(message)));
    }
}
