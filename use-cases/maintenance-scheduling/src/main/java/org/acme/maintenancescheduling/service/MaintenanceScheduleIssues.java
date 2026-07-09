package org.acme.maintenancescheduling.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.maintenancescheduling.dto.CrewIdDetail;
import org.acme.maintenancescheduling.dto.JobIdDetail;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleValidationIssue;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class MaintenanceScheduleIssues {

    private MaintenanceScheduleIssues() {
    }

    public abstract static class MaintenanceScheduleIssue extends AbstractIssue {
        protected MaintenanceScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class JobIdMissingIssue extends MaintenanceScheduleIssue {
        private static final IssueType TYPE = MaintenanceScheduleValidationIssue.JOB_ID_MISSING.asIssueType();

        public JobIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateJobIdIssue extends MaintenanceScheduleIssue {
        private static final IssueType TYPE = MaintenanceScheduleValidationIssue.DUPLICATE_JOB_ID.asIssueType();

        public DuplicateJobIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateJobIdIssue(JobIdDetail jobIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(jobIdDetail)).toList());
        }
    }

    public static final class CrewIdMissingIssue extends MaintenanceScheduleIssue {
        private static final IssueType TYPE = MaintenanceScheduleValidationIssue.CREW_ID_MISSING.asIssueType();

        public CrewIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateCrewIdIssue extends MaintenanceScheduleIssue {
        private static final IssueType TYPE = MaintenanceScheduleValidationIssue.DUPLICATE_CREW_ID.asIssueType();

        public DuplicateCrewIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateCrewIdIssue(CrewIdDetail crewIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(crewIdDetail)).toList());
        }
    }

    public static final class WorkCalendarMissingIssue extends MaintenanceScheduleIssue {
        private static final IssueType TYPE = MaintenanceScheduleValidationIssue.WORK_CALENDAR_MISSING.asIssueType();

        public WorkCalendarMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class NonExistingCrewReferenceIssue extends MaintenanceScheduleIssue {
        private static final IssueType TYPE = MaintenanceScheduleValidationIssue.NON_EXISTING_CREW_REFERENCE.asIssueType();

        public NonExistingCrewReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingCrewReferenceIssue(JobIdDetail jobIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(jobIdDetail)).toList());
        }
    }
}
