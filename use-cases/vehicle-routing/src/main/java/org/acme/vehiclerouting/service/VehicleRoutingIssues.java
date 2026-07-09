package org.acme.vehiclerouting.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.vehiclerouting.dto.VehicleIdDetail;
import org.acme.vehiclerouting.dto.VehicleRoutingValidationIssue;
import org.acme.vehiclerouting.dto.VisitIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class VehicleRoutingIssues {

    private VehicleRoutingIssues() {
    }

    public abstract static class VehicleRoutingIssue extends AbstractIssue {
        protected VehicleRoutingIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class VisitIdMissingIssue extends VehicleRoutingIssue {
        private static final IssueType TYPE = VehicleRoutingValidationIssue.VISIT_ID_MISSING.asIssueType();

        public VisitIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateVisitIdIssue extends VehicleRoutingIssue {
        private static final IssueType TYPE = VehicleRoutingValidationIssue.DUPLICATE_VISIT_ID.asIssueType();

        public DuplicateVisitIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateVisitIdIssue(VisitIdDetail visitIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(visitIdDetail)).toList());
        }
    }

    public static final class VehicleIdMissingIssue extends VehicleRoutingIssue {
        private static final IssueType TYPE = VehicleRoutingValidationIssue.VEHICLE_ID_MISSING.asIssueType();

        public VehicleIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateVehicleIdIssue extends VehicleRoutingIssue {
        private static final IssueType TYPE = VehicleRoutingValidationIssue.DUPLICATE_VEHICLE_ID.asIssueType();

        public DuplicateVehicleIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateVehicleIdIssue(VehicleIdDetail vehicleIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(vehicleIdDetail)).toList());
        }
    }

    public static final class NonExistingVisitReferenceIssue extends VehicleRoutingIssue {
        private static final IssueType TYPE = VehicleRoutingValidationIssue.NON_EXISTING_VISIT_REFERENCE.asIssueType();

        public NonExistingVisitReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingVisitReferenceIssue(VisitIdDetail visitIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(visitIdDetail)).toList());
        }
    }

    public static final class DuplicateVisitAssignmentIssue extends VehicleRoutingIssue {
        private static final IssueType TYPE = VehicleRoutingValidationIssue.DUPLICATE_VISIT_ASSIGNMENT.asIssueType();

        public DuplicateVisitAssignmentIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateVisitAssignmentIssue(VisitIdDetail visitIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(visitIdDetail)).toList());
        }
    }
}
