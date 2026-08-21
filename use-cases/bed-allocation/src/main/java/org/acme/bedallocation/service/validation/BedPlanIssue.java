package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a bed allocation input.",
        oneOf = {
                DepartmentIdMissingIssue.class,
                DuplicateDepartmentIdIssue.class,
                RoomIdMissingIssue.class,
                DuplicateRoomIdIssue.class,
                BedIdMissingIssue.class,
                DuplicateBedIdIssue.class,
                StayIdMissingIssue.class,
                DuplicateStayIdIssue.class,
                NonExistingBedReferenceIssue.class
        })
public abstract class BedPlanIssue extends AbstractIssue {

    protected BedPlanIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }
}
