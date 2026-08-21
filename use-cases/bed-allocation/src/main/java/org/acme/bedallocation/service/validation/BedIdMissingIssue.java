package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(allOf = { BedPlanIssue.class })
public class BedIdMissingIssue extends BedPlanIssue {

    public static final IssueCode ISSUE_CODE = IssueCode.of("BED_ID_MISSING");
    public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Bed ID must not be null or blank.");

    public BedIdMissingIssue() {
        super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
    }
}
