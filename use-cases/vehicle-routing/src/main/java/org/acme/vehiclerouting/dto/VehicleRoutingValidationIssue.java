package org.acme.vehiclerouting.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a vehicle routing problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum VehicleRoutingValidationIssue {
    VISIT_ID_MISSING(IssueCode.of("VISIT_ID_MISSING"), IssueSeverity.ERROR,
            "Visit ID must not be null or blank."),
    DUPLICATE_VISIT_ID(IssueCode.of("DUPLICATE_VISIT_ID"), IssueSeverity.ERROR,
            "Duplicate visit ID found."),
    VEHICLE_ID_MISSING(IssueCode.of("VEHICLE_ID_MISSING"), IssueSeverity.ERROR,
            "Vehicle ID must not be null or blank."),
    DUPLICATE_VEHICLE_ID(IssueCode.of("DUPLICATE_VEHICLE_ID"), IssueSeverity.ERROR,
            "Duplicate vehicle ID found."),
    NON_EXISTING_VISIT_REFERENCE(IssueCode.of("NON_EXISTING_VISIT_REFERENCE"), IssueSeverity.ERROR,
            "Vehicle references a non-existing visit."),
    DUPLICATE_VISIT_ASSIGNMENT(IssueCode.of("DUPLICATE_VISIT_ASSIGNMENT"), IssueSeverity.ERROR,
            "Visit is assigned to more than one vehicle.");

    private final transient IssueType issueType;

    VehicleRoutingValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
