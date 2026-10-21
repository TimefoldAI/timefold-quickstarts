package org.acme.orderpicking.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for an order picking input.",
        oneOf = {
                PickingPlanIssue.DuplicateProductIdIssue.class,
                PickingPlanIssue.DuplicatePickTaskIdIssue.class,
                PickingPlanIssue.DuplicatePickingCartIdIssue.class,
                PickingPlanIssue.UnknownRackIssue.class,
                PickingPlanIssue.NonExistingProductReferenceIssue.class,
                PickingPlanIssue.NonExistingPickTaskReferenceIssue.class,
                PickingPlanIssue.PickTaskAssignedMoreThanOnceIssue.class,
                PickingPlanIssue.ProductExceedsBucketCapacityIssue.class
        })
public abstract class PickingPlanIssue extends AbstractIssue {

    protected PickingPlanIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { PickingPlanIssue.class })
    public static class DuplicateProductIdIssue extends PickingPlanIssue {

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

    @Schema(allOf = { PickingPlanIssue.class })
    public static class DuplicatePickTaskIdIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_PICK_TASK_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate pick task ID found.");

        @Schema(description = "The ID of the duplicated pick task.")
        private String pickTaskId;

        public DuplicatePickTaskIdIssue() {
            this(null);
        }

        public DuplicatePickTaskIdIssue(String pickTaskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.pickTaskId = pickTaskId;
        }

        public String getPickTaskId() {
            return pickTaskId;
        }
    }

    @Schema(allOf = { PickingPlanIssue.class })
    public static class DuplicatePickingCartIdIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_PICKING_CART_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate picking cart ID found.");

        @Schema(description = "The ID of the duplicated picking cart.")
        private String pickingCartId;

        public DuplicatePickingCartIdIssue() {
            this(null);
        }

        public DuplicatePickingCartIdIssue(String pickingCartId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.pickingCartId = pickingCartId;
        }

        public String getPickingCartId() {
            return pickingCartId;
        }
    }

    /**
     * The warehouse structure is fixed (racks A1 to E3), and the travel distance between two locations can only be
     * calculated for racks that exist in it. Reported once per unknown rack ID, however many products or
     * picking carts refer to it.
     */
    @Schema(allOf = { PickingPlanIssue.class })
    public static class UnknownRackIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("UNKNOWN_RACK");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Location refers to a rack that does not exist in the warehouse.");

        @Schema(description = "The unknown rack ID.")
        private String rackId;

        public UnknownRackIssue() {
            this(null);
        }

        public UnknownRackIssue(String rackId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.rackId = rackId;
        }

        public String getRackId() {
            return rackId;
        }
    }

    @Schema(allOf = { PickingPlanIssue.class })
    public static class NonExistingProductReferenceIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_PRODUCT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Pick task refers to a product ID that does not exist.");

        @Schema(description = "The ID of the pick task with the unknown product reference.")
        private String pickTaskId;

        public NonExistingProductReferenceIssue() {
            this(null);
        }

        public NonExistingProductReferenceIssue(String pickTaskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.pickTaskId = pickTaskId;
        }

        public String getPickTaskId() {
            return pickTaskId;
        }
    }

    /**
     * Reported for the first unknown pick task ID in a picking cart's path only, so the issue list stays bounded by the
     * number of picking carts.
     */
    @Schema(allOf = { PickingPlanIssue.class })
    public static class NonExistingPickTaskReferenceIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_PICK_TASK_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Picking cart refers to a pick task ID that does not exist.");

        @Schema(description = "The ID of the picking cart with the unknown pick task reference.")
        private String pickingCartId;
        @Schema(description = "The unknown pick task ID.")
        private String pickTaskId;

        public NonExistingPickTaskReferenceIssue() {
            this(null, null);
        }

        public NonExistingPickTaskReferenceIssue(String pickingCartId, String pickTaskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.pickingCartId = pickingCartId;
            this.pickTaskId = pickTaskId;
        }

        public String getPickingCartId() {
            return pickingCartId;
        }

        public String getPickTaskId() {
            return pickTaskId;
        }
    }

    /**
     * A picking cart's path is a list variable, in which a pick task can only take one position: it cannot be picked
     * twice, nor by two picking carts.
     */
    @Schema(allOf = { PickingPlanIssue.class })
    public static class PickTaskAssignedMoreThanOnceIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("PICK_TASK_ASSIGNED_MORE_THAN_ONCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Pick task appears more than once in the picking carts' paths.");

        @Schema(description = "The ID of the pick task that appears more than once.")
        private String pickTaskId;

        public PickTaskAssignedMoreThanOnceIssue() {
            this(null);
        }

        public PickTaskAssignedMoreThanOnceIssue(String pickTaskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.pickTaskId = pickTaskId;
        }

        public String getPickTaskId() {
            return pickTaskId;
        }
    }

    /**
     * For simplicity, the model assumes every product fits in a picking cart bucket. A product that is bigger than the
     * buckets of every picking cart breaks that assumption.
     */
    @Schema(allOf = { PickingPlanIssue.class })
    public static class ProductExceedsBucketCapacityIssue extends PickingPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("PRODUCT_EXCEEDS_BUCKET_CAPACITY");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Product does not fit in the bucket of any picking cart.");

        @Schema(description = "The ID of the product that is too big.")
        private String productId;
        @Schema(description = "The volume of the product in cm3.")
        private int volume;
        @Schema(description = "The largest bucket capacity of all picking carts in cm3.")
        private int bucketCapacity;

        public ProductExceedsBucketCapacityIssue() {
            this(null, 0, 0);
        }

        public ProductExceedsBucketCapacityIssue(String productId, int volume, int bucketCapacity) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.productId = productId;
            this.volume = volume;
            this.bucketCapacity = bucketCapacity;
        }

        public String getProductId() {
            return productId;
        }

        public int getVolume() {
            return volume;
        }

        public int getBucketCapacity() {
            return bucketCapacity;
        }
    }
}
