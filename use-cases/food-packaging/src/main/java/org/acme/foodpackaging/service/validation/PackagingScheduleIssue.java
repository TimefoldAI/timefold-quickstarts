package org.acme.foodpackaging.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a food packaging input.",
        oneOf = {
                PackagingScheduleIssue.DuplicateProductIdIssue.class,
                PackagingScheduleIssue.DuplicateOperatorIdIssue.class,
                PackagingScheduleIssue.DuplicateLineIdIssue.class,
                PackagingScheduleIssue.DuplicateJobIdIssue.class,
                PackagingScheduleIssue.NonExistingProductReferenceIssue.class,
                PackagingScheduleIssue.NonExistingOperatorReferenceIssue.class,
                PackagingScheduleIssue.NonExistingJobReferenceIssue.class,
                PackagingScheduleIssue.MissingCleaningDurationIssue.class,
                PackagingScheduleIssue.JobOnMultipleLinesIssue.class
        })
public abstract class PackagingScheduleIssue extends AbstractIssue {

    protected PackagingScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class DuplicateProductIdIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_PRODUCT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate product ID found.");

        @Schema(description = "The ID of the duplicated product.")
        private String productId;

        public DuplicateProductIdIssue() {
            this(null);
        }

        public DuplicateProductIdIssue(String productId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.productId = productId;
        }

        public String getProductId() {
            return productId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class DuplicateOperatorIdIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_OPERATOR_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate operator ID found.");

        @Schema(description = "The ID of the duplicated operator.")
        private String operatorId;

        public DuplicateOperatorIdIssue() {
            this(null);
        }

        public DuplicateOperatorIdIssue(String operatorId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.operatorId = operatorId;
        }

        public String getOperatorId() {
            return operatorId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class DuplicateLineIdIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_LINE_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate line ID found.");

        @Schema(description = "The ID of the duplicated line.")
        private String lineId;

        public DuplicateLineIdIssue() {
            this(null);
        }

        public DuplicateLineIdIssue(String lineId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lineId = lineId;
        }

        public String getLineId() {
            return lineId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class DuplicateJobIdIssue extends PackagingScheduleIssue {

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

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class NonExistingProductReferenceIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_PRODUCT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Reference to a non-existing product found.");

        @Schema(description = "The ID of the product that does not exist.")
        private String productId;

        public NonExistingProductReferenceIssue() {
            this(null);
        }

        public NonExistingProductReferenceIssue(String productId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.productId = productId;
        }

        public String getProductId() {
            return productId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class NonExistingOperatorReferenceIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_OPERATOR_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Line references a non-existing operator.");

        @Schema(description = "The ID of the line with the dangling reference.")
        private String lineId;

        public NonExistingOperatorReferenceIssue() {
            this(null);
        }

        public NonExistingOperatorReferenceIssue(String lineId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lineId = lineId;
        }

        public String getLineId() {
            return lineId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class NonExistingJobReferenceIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_JOB_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Line references a non-existing job.");

        @Schema(description = "The ID of the line with the dangling reference.")
        private String lineId;

        public NonExistingJobReferenceIssue() {
            this(null);
        }

        public NonExistingJobReferenceIssue(String lineId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.lineId = lineId;
        }

        public String getLineId() {
            return lineId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class MissingCleaningDurationIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("MISSING_CLEANING_DURATION");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "Product is missing the cleaning duration for a product that could be produced before it.");

        @Schema(description = "The ID of the product that is missing a cleaning duration.")
        private String productId;

        @Schema(description = "The ID of the previous product the cleaning duration is missing for.")
        private String previousProductId;

        public MissingCleaningDurationIssue() {
            this(null, null);
        }

        public MissingCleaningDurationIssue(String productId, String previousProductId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.productId = productId;
            this.previousProductId = previousProductId;
        }

        public String getProductId() {
            return productId;
        }

        public String getPreviousProductId() {
            return previousProductId;
        }
    }

    @Schema(allOf = { PackagingScheduleIssue.class })
    public static class JobOnMultipleLinesIssue extends PackagingScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("JOB_ON_MULTIPLE_LINES");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "Job is already scheduled on more than one line.");

        @Schema(description = "The ID of the job scheduled on more than one line.")
        private String jobId;

        public JobOnMultipleLinesIssue() {
            this(null);
        }

        public JobOnMultipleLinesIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }
}
