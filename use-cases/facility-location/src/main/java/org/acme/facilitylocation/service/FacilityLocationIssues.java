package org.acme.facilitylocation.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.facilitylocation.dto.ConsumerIdDetail;
import org.acme.facilitylocation.dto.FacilityIdDetail;
import org.acme.facilitylocation.dto.FacilityLocationValidationIssue;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class FacilityLocationIssues {

    private FacilityLocationIssues() {
    }

    public abstract static class FacilityLocationIssue extends AbstractIssue {
        protected FacilityLocationIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class FacilityIdMissingIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.FACILITY_ID_MISSING.asIssueType();

        public FacilityIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateFacilityIdIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.DUPLICATE_FACILITY_ID.asIssueType();

        public DuplicateFacilityIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateFacilityIdIssue(FacilityIdDetail facilityIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(facilityIdDetail)).toList());
        }
    }

    public static final class NegativeFacilityCapacityIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.NEGATIVE_FACILITY_CAPACITY.asIssueType();

        public NegativeFacilityCapacityIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NegativeFacilityCapacityIssue(FacilityIdDetail facilityIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(facilityIdDetail)).toList());
        }
    }

    public static final class NegativeFacilitySetupCostIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.NEGATIVE_FACILITY_SETUP_COST.asIssueType();

        public NegativeFacilitySetupCostIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NegativeFacilitySetupCostIssue(FacilityIdDetail facilityIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(facilityIdDetail)).toList());
        }
    }

    public static final class ConsumerIdMissingIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.CONSUMER_ID_MISSING.asIssueType();

        public ConsumerIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateConsumerIdIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.DUPLICATE_CONSUMER_ID.asIssueType();

        public DuplicateConsumerIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateConsumerIdIssue(ConsumerIdDetail consumerIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(consumerIdDetail)).toList());
        }
    }

    public static final class NegativeConsumerDemandIssue extends FacilityLocationIssue {
        private static final IssueType TYPE = FacilityLocationValidationIssue.NEGATIVE_CONSUMER_DEMAND.asIssueType();

        public NegativeConsumerDemandIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NegativeConsumerDemandIssue(ConsumerIdDetail consumerIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(consumerIdDetail)).toList());
        }
    }

    public static final class NonExistingFacilityReferenceIssue extends FacilityLocationIssue {
        private static final IssueType TYPE =
                FacilityLocationValidationIssue.NON_EXISTING_FACILITY_REFERENCE.asIssueType();

        public NonExistingFacilityReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingFacilityReferenceIssue(ConsumerIdDetail consumerIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(consumerIdDetail)).toList());
        }
    }
}
