package org.acme.maintenancescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a maintenance scheduling input.",
        oneOf = {
                MaintenanceScheduleIssue.DuplicateCrewIdIssue.class,
                MaintenanceScheduleIssue.DuplicateJobIdIssue.class,
                MaintenanceScheduleIssue.NonExistingCrewReferenceIssue.class,
                MaintenanceScheduleIssue.EmptyWorkCalendarIssue.class,
                MaintenanceScheduleIssue.JobWindowTooShortIssue.class,
                MaintenanceScheduleIssue.StartDateOutsideWorkCalendarIssue.class
        })
public abstract class MaintenanceScheduleIssue extends AbstractIssue {

    protected MaintenanceScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { MaintenanceScheduleIssue.class })
    public static class DuplicateCrewIdIssue extends MaintenanceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_CREW_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate crew ID found.");

        @Schema(description = "The ID of the duplicated crew.")
        private String crewId;

        public DuplicateCrewIdIssue() {
            this(null);
        }

        public DuplicateCrewIdIssue(String crewId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.crewId = crewId;
        }

        public String getCrewId() {
            return crewId;
        }
    }

    @Schema(allOf = { MaintenanceScheduleIssue.class })
    public static class DuplicateJobIdIssue extends MaintenanceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_JOB_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate job ID found.");

        @Schema(description = "The ID of the duplicated job.")
        private String jobId;

        public DuplicateJobIdIssue() {
            this(null);
        }

        public DuplicateJobIdIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }

    @Schema(allOf = { MaintenanceScheduleIssue.class })
    public static class NonExistingCrewReferenceIssue extends MaintenanceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_CREW_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Job refers to a crew ID that does not exist.");

        @Schema(description = "The ID of the job with the unknown crew reference.")
        private String jobId;

        public NonExistingCrewReferenceIssue() {
            this(null);
        }

        public NonExistingCrewReferenceIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }

    @Schema(allOf = { MaintenanceScheduleIssue.class })
    public static class EmptyWorkCalendarIssue extends MaintenanceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("EMPTY_WORK_CALENDAR");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("The work calendar contains no workday to schedule jobs on.");

        @Schema(description = "The ID of the empty work calendar.")
        private String workCalendarId;

        public EmptyWorkCalendarIssue() {
            this(null);
        }

        public EmptyWorkCalendarIssue(String workCalendarId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.workCalendarId = workCalendarId;
        }

        public String getWorkCalendarId() {
            return workCalendarId;
        }
    }

    @Schema(allOf = { MaintenanceScheduleIssue.class })
    public static class JobWindowTooShortIssue extends MaintenanceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("JOB_WINDOW_TOO_SHORT");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "The window between the job's minimum start date and maximum end date is too short for its duration.");

        @Schema(description = "The ID of the job whose window is too short.")
        private String jobId;

        public JobWindowTooShortIssue() {
            this(null);
        }

        public JobWindowTooShortIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }

    @Schema(allOf = { MaintenanceScheduleIssue.class })
    public static class StartDateOutsideWorkCalendarIssue extends MaintenanceScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("START_DATE_OUTSIDE_WORK_CALENDAR");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("The job's start date is not a workday inside the work calendar.");

        @Schema(description = "The ID of the job with the out-of-range start date.")
        private String jobId;

        public StartDateOutsideWorkCalendarIssue() {
            this(null);
        }

        public StartDateOutsideWorkCalendarIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }
}
