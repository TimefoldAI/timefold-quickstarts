package org.acme.vehiclerouting.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a vehicle routing input.",
        oneOf = {
                VehicleRoutePlanIssue.DuplicateVehicleIdIssue.class,
                VehicleRoutePlanIssue.DuplicateVisitIdIssue.class,
                VehicleRoutePlanIssue.NonExistingVisitReferenceIssue.class,
                VehicleRoutePlanIssue.VisitAssignedMoreThanOnceIssue.class,
                VehicleRoutePlanIssue.VisitWindowTooShortIssue.class,
                VehicleRoutePlanIssue.InvalidMapBoundsIssue.class
        })
public abstract class VehicleRoutePlanIssue extends AbstractIssue {

    protected VehicleRoutePlanIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { VehicleRoutePlanIssue.class })
    public static class DuplicateVehicleIdIssue extends VehicleRoutePlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_VEHICLE_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate vehicle ID found.");

        @Schema(description = "The ID of the duplicated vehicle.")
        private String vehicleId;

        public DuplicateVehicleIdIssue() {
            this(null);
        }

        public DuplicateVehicleIdIssue(String vehicleId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.vehicleId = vehicleId;
        }

        public String getVehicleId() {
            return vehicleId;
        }
    }

    @Schema(allOf = { VehicleRoutePlanIssue.class })
    public static class DuplicateVisitIdIssue extends VehicleRoutePlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_VISIT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate visit ID found.");

        @Schema(description = "The ID of the duplicated visit.")
        private String visitId;

        public DuplicateVisitIdIssue() {
            this(null);
        }

        public DuplicateVisitIdIssue(String visitId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.visitId = visitId;
        }

        public String getVisitId() {
            return visitId;
        }
    }

    @Schema(allOf = { VehicleRoutePlanIssue.class })
    public static class NonExistingVisitReferenceIssue extends VehicleRoutePlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_VISIT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("The route of a vehicle refers to a visit ID that does not exist.");

        @Schema(description = "The ID of the vehicle whose route holds the unknown visit reference.")
        private String vehicleId;
        @Schema(description = "The unknown visit ID.")
        private String visitId;

        public NonExistingVisitReferenceIssue() {
            this(null, null);
        }

        public NonExistingVisitReferenceIssue(String vehicleId, String visitId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.vehicleId = vehicleId;
            this.visitId = visitId;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public String getVisitId() {
            return visitId;
        }
    }

    @Schema(allOf = { VehicleRoutePlanIssue.class })
    public static class VisitAssignedMoreThanOnceIssue extends VehicleRoutePlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("VISIT_ASSIGNED_MORE_THAN_ONCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "The visit appears in more than one vehicle route, or twice in the same one, but it can only be "
                        + "serviced once.");

        @Schema(description = "The ID of the visit that is assigned more than once.")
        private String visitId;

        public VisitAssignedMoreThanOnceIssue() {
            this(null);
        }

        public VisitAssignedMoreThanOnceIssue(String visitId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.visitId = visitId;
        }

        public String getVisitId() {
            return visitId;
        }
    }

    @Schema(allOf = { VehicleRoutePlanIssue.class })
    public static class VisitWindowTooShortIssue extends VehicleRoutePlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("VISIT_WINDOW_TOO_SHORT");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "The time window between the visit's earliest start time and its maximum end time is too short for "
                        + "its service duration.");

        @Schema(description = "The ID of the visit whose time window is too short.")
        private String visitId;

        public VisitWindowTooShortIssue() {
            this(null);
        }

        public VisitWindowTooShortIssue(String visitId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.visitId = visitId;
        }

        public String getVisitId() {
            return visitId;
        }
    }

    @Schema(allOf = { VehicleRoutePlanIssue.class })
    public static class InvalidMapBoundsIssue extends VehicleRoutePlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INVALID_MAP_BOUNDS");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "The north-east corner of the map area must lie strictly north-east of the south-west corner.");

        public InvalidMapBoundsIssue() {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        }
    }
}
