package org.acme.facilitylocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a facility location input.",
        oneOf = {
                FacilityPlanIssue.DuplicateFacilityIdIssue.class,
                FacilityPlanIssue.DuplicateConsumerIdIssue.class,
                FacilityPlanIssue.NonExistingFacilityReferenceIssue.class,
                FacilityPlanIssue.InsufficientTotalCapacityIssue.class
        })
public abstract class FacilityPlanIssue extends AbstractIssue {

    protected FacilityPlanIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { FacilityPlanIssue.class })
    public static class DuplicateFacilityIdIssue extends FacilityPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_FACILITY_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate facility ID found.");

        @Schema(description = "The ID of the duplicated facility.")
        private String facilityId;

        public DuplicateFacilityIdIssue() {
            this(null);
        }

        public DuplicateFacilityIdIssue(String facilityId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.facilityId = facilityId;
        }

        public String getFacilityId() {
            return facilityId;
        }
    }

    @Schema(allOf = { FacilityPlanIssue.class })
    public static class DuplicateConsumerIdIssue extends FacilityPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_CONSUMER_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate consumer ID found.");

        @Schema(description = "The ID of the duplicated consumer.")
        private String consumerId;

        public DuplicateConsumerIdIssue() {
            this(null);
        }

        public DuplicateConsumerIdIssue(String consumerId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.consumerId = consumerId;
        }

        public String getConsumerId() {
            return consumerId;
        }
    }

    @Schema(allOf = { FacilityPlanIssue.class })
    public static class NonExistingFacilityReferenceIssue extends FacilityPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_FACILITY_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Consumer refers to a facility ID that does not exist.");

        @Schema(description = "The ID of the consumer with the unknown facility reference.")
        private String consumerId;

        public NonExistingFacilityReferenceIssue() {
            this(null);
        }

        public NonExistingFacilityReferenceIssue(String consumerId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.consumerId = consumerId;
        }

        public String getConsumerId() {
            return consumerId;
        }
    }

    /**
     * Every consumer must be served by a facility, so a dataset whose facilities cannot together absorb the total
     * demand has no feasible solution at all. Reporting that up front beats letting the solver spend its entire
     * time budget on a problem that can never reach a feasible score.
     */
    @Schema(allOf = { FacilityPlanIssue.class })
    public static class InsufficientTotalCapacityIssue extends FacilityPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INSUFFICIENT_TOTAL_CAPACITY");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("The total facility capacity is below the total consumer demand.");

        @Schema(description = "The cumulative capacity of every facility in the dataset.")
        private long totalCapacity;
        @Schema(description = "The cumulative demand of every consumer in the dataset.")
        private long totalDemand;

        public InsufficientTotalCapacityIssue() {
            this(0, 0);
        }

        public InsufficientTotalCapacityIssue(long totalCapacity, long totalDemand) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.totalCapacity = totalCapacity;
            this.totalDemand = totalDemand;
        }

        public long getTotalCapacity() {
            return totalCapacity;
        }

        public long getTotalDemand() {
            return totalDemand;
        }
    }
}
