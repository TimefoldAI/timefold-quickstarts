package org.acme.bedallocation.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.bedallocation.dto.BedIdDetail;
import org.acme.bedallocation.dto.BedScheduleValidationIssue;
import org.acme.bedallocation.dto.StayIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class BedScheduleIssues {

    private BedScheduleIssues() {
    }

    public abstract static class BedScheduleIssue extends AbstractIssue {
        protected BedScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class StayIdMissingIssue extends BedScheduleIssue {
        private static final IssueType TYPE = BedScheduleValidationIssue.STAY_ID_MISSING.asIssueType();

        public StayIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateStayIdIssue extends BedScheduleIssue {
        private static final IssueType TYPE = BedScheduleValidationIssue.DUPLICATE_STAY_ID.asIssueType();

        public DuplicateStayIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateStayIdIssue(StayIdDetail stayIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(stayIdDetail)).toList());
        }
    }

    public static final class BedIdMissingIssue extends BedScheduleIssue {
        private static final IssueType TYPE = BedScheduleValidationIssue.BED_ID_MISSING.asIssueType();

        public BedIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateBedIdIssue extends BedScheduleIssue {
        private static final IssueType TYPE = BedScheduleValidationIssue.DUPLICATE_BED_ID.asIssueType();

        public DuplicateBedIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateBedIdIssue(BedIdDetail bedIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(bedIdDetail)).toList());
        }
    }

    public static final class NonExistingBedReferenceIssue extends BedScheduleIssue {
        private static final IssueType TYPE = BedScheduleValidationIssue.NON_EXISTING_BED_REFERENCE.asIssueType();

        public NonExistingBedReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingBedReferenceIssue(StayIdDetail stayIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(stayIdDetail)).toList());
        }
    }
}
