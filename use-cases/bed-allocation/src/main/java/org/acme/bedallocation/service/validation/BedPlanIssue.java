package org.acme.bedallocation.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a bed allocation input.",
        oneOf = {
                BedPlanIssue.DepartmentIdMissingIssue.class,
                BedPlanIssue.DuplicateDepartmentIdIssue.class,
                BedPlanIssue.RoomIdMissingIssue.class,
                BedPlanIssue.DuplicateRoomIdIssue.class,
                BedPlanIssue.BedIdMissingIssue.class,
                BedPlanIssue.DuplicateBedIdIssue.class,
                BedPlanIssue.StayIdMissingIssue.class,
                BedPlanIssue.DuplicateStayIdIssue.class,
                BedPlanIssue.NonExistingBedReferenceIssue.class
        })
public abstract class BedPlanIssue extends AbstractIssue {

    protected BedPlanIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class DepartmentIdMissingIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DEPARTMENT_ID_MISSING");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Department ID must not be null or blank.");

        public DepartmentIdMissingIssue() {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class DuplicateDepartmentIdIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_DEPARTMENT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate department ID found.");

        @Schema(description = "The ID of the duplicated department.")
        private String departmentId;

        public DuplicateDepartmentIdIssue() {
            this(null);
        }

        public DuplicateDepartmentIdIssue(String departmentId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.departmentId = departmentId;
        }

        public String getDepartmentId() {
            return departmentId;
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class RoomIdMissingIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("ROOM_ID_MISSING");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Room ID must not be null or blank.");

        public RoomIdMissingIssue() {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class DuplicateRoomIdIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_ROOM_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate room ID found.");

        @Schema(description = "The ID of the duplicated room.")
        private String roomId;

        public DuplicateRoomIdIssue() {
            this(null);
        }

        public DuplicateRoomIdIssue(String roomId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.roomId = roomId;
        }

        public String getRoomId() {
            return roomId;
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class BedIdMissingIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("BED_ID_MISSING");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Bed ID must not be null or blank.");

        public BedIdMissingIssue() {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class DuplicateBedIdIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_BED_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate bed ID found.");

        @Schema(description = "The ID of the duplicated bed.")
        private String bedId;

        public DuplicateBedIdIssue() {
            this(null);
        }

        public DuplicateBedIdIssue(String bedId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.bedId = bedId;
        }

        public String getBedId() {
            return bedId;
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class StayIdMissingIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("STAY_ID_MISSING");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Stay ID must not be null or blank.");

        public StayIdMissingIssue() {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class DuplicateStayIdIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_STAY_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate stay ID found.");

        @Schema(description = "The ID of the duplicated stay.")
        private String stayId;

        public DuplicateStayIdIssue() {
            this(null);
        }

        public DuplicateStayIdIssue(String stayId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.stayId = stayId;
        }

        public String getStayId() {
            return stayId;
        }
    }

    @Schema(allOf = { BedPlanIssue.class })
    public static class NonExistingBedReferenceIssue extends BedPlanIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_BED_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Stay refers to a bed ID that does not exist.");

        @Schema(description = "The ID of the stay with the unknown bed reference.")
        private String stayId;

        public NonExistingBedReferenceIssue() {
            this(null);
        }

        public NonExistingBedReferenceIssue(String stayId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.stayId = stayId;
        }

        public String getStayId() {
            return stayId;
        }
    }
}
